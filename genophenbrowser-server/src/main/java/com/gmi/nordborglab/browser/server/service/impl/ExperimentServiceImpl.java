package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.es.ESTermsFacet;
import com.gmi.nordborglab.browser.server.domain.DomainFunctions;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.domain.pages.PublicationPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.es.EsIndexer;
import com.gmi.nordborglab.browser.server.es.EsSearcher;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.PublicationRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.search.PublicationSearchProcessor;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

@Service
@Transactional(readOnly = true)
public class ExperimentServiceImpl extends WebApplicationObjectSupport
        implements ExperimentService {

    @Resource
    private ExperimentRepository experimentRepository;

    @Resource
    private PublicationRepository publicationRepository;

    @Resource
    private TraitUomService traitUomService;

    @Resource
    private TraitUomRepository traitUomRepository;

    @Resource
    private AclManager aclManager;

    @Resource
    private RoleHierarchy roleHierarchy;

    @Resource
    private Client client;

    @Resource
    private EsIndexer esIndexer;

    @Resource
    private EsSearcher esSearcher;


    @Resource
    private EsAclManager esAclManager;

    @Resource
    private TermRepository termRepository;

    @Resource
    PermissionFactory permissionFactory;


    @Transactional(readOnly = false)
    @Override
    public Experiment save(Experiment experiment) {
        boolean isNewRecord = experiment.getId() == null;
        experiment = experimentRepository.save(experiment);
        if (isNewRecord) {
            CumulativePermission permission = new CumulativePermission();
            permission.set(CustomPermission.ADMINISTRATION);
            permission.set(CustomPermission.EDIT);
            permission.set(CustomPermission.READ);
            aclManager.addPermission(experiment, new PrincipalSid(SecurityUtil.getUsername()),
                    permission, null);
            aclManager.addPermission(experiment, new GrantedAuthoritySid("ROLE_ADMIN"), permission, null);
        }
        indexExperiment(experiment);
        experiment = aclManager.setPermissionAndOwner(experiment);
        return experiment;
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Experiment experiment) {
        aclManager.setPermissionAndOwner(experiment);
        //check not public
        if (experiment.isPublic()) {
            throw new RuntimeException("Public studies can't be deleted");
        }
        List<TraitUom> traitUoms = traitUomRepository.findByExperimentId(experiment.getId());
        for (TraitUom traitUom : traitUoms) {
            traitUomService.delete(traitUom);
        }
        experimentRepository.delete(experiment);
        aclManager.deletePermissions(experiment, true);
        deleteFromIndex(experiment);
    }

    private void deleteFromIndex(Experiment experiment) {
        esIndexer.delete(experiment);
    }

    private void indexExperiment(Experiment experiment) {
        try {
            esIndexer.index(experiment);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Transactional(readOnly = false)
    @Override
    public Experiment saveExperimentUploadData(@Valid ExperimentUploadData data) {

        checkNotNull(data.getSampleData());
        checkNotNull(data.getExperiment());
        checkNotNull(data.getPhenotypes());
        Experiment experiment = data.getExperiment();
        if (experiment.getId() == null) {
            experiment = save(experiment);
        }
        // acd DOI
        //add phenotypes
        traitUomService.savePhenotypeUploadData(experiment, data.getPhenotypes(), data.getSampleData());
        return experiment;
    }

    @Override
    public ExperimentPage findByAclAndFilter(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size) {
        SearchResponse response = esSearcher.search(filter, null, false, new String[]{"name^3.5", "name.partial^1.5", "originator", "design", "comments^0.5", "owner.name"}, searchString, Experiment.ES_TYPE, page, size);
        List<Long> idsToFetch = EsSearcher.getIdsFromResponse(response);
        List<Experiment> resultsFromDb = experimentRepository.findAll(idsToFetch);
        //extract facets
        List<ESFacet> facets = EsSearcher.getAggregations(response);
        Ordering<Experiment> orderByEs = Ordering.explicit(idsToFetch).onResultOf(DomainFunctions.getExperimentId());
        List<Experiment> results = orderByEs.immutableSortedCopy(resultsFromDb);
        aclManager.setPermissionAndOwners(results);
        return new ExperimentPage(results, new PageRequest(page, size), response.getHits().getTotalHits(), facets);
    }


    @Override
    public List<Experiment> findAllByAcl(Integer permission) {
        List<String> authorities = SecurityUtil.getAuthorities(roleHierarchy);
        List<Experiment> experiments = experimentRepository.findAll();
        experiments = aclManager.filterByAcl(experiments, Lists.newArrayList(permissionFactory.buildFromMask(permission))).toList();
        for (Experiment experiment : experiments) {
            experiment.setNumberOfPhenotypes(traitUomService.countPhenotypeByExperimentCount(experiment.getId()));
        }
        aclManager.setPermissionAndOwners(experiments);
        return experiments;
    }


    @Override
    public Experiment findExperiment(Long id) {
        Experiment experiment = experimentRepository.findOne(id);
        experiment = aclManager.setPermissionAndOwner(experiment);
        experiment.setNumberOfPhenotypes(traitUomService.countPhenotypeByExperimentCount(experiment.getId()));
        experiment.setNumberOfAnalyses(countAnalysesByExperiment(id));
        experiment.setStats(getPhenotypeStats(id));
        return experiment;
    }

    @Transactional(readOnly = false)
    @Override
    public Experiment addPublication(Long id, Publication publication) {
        Experiment experiment = experimentRepository.findOne(id);
        Publication existing = publicationRepository.findByDoi(publication.getDOI());
        if (existing == null) {
            existing = publication;
        }
        if (experiment.getPublications().contains(existing))
            return experiment;
        experiment.addPublication(existing);
        return experiment;
    }

    @Override
    public PublicationPage getPublications(String searchString, int start, int size) {
        PublicationSearchProcessor publicationSearchProcessor = new PublicationSearchProcessor(searchString);
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request = publicationSearchProcessor.getSearchBuilder(request);
        if (searchString == null || searchString.equalsIgnoreCase("")) {
            request.setQuery(matchAllQuery());
        }
        request.setSize(size).setFrom(start).setNoFields();
        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<Publication> publications = Lists.newArrayList();
        //Neded because ids are not sorted
        Map<Long, Publication> id2Map = Maps.uniqueIndex(publicationRepository.findAll(idsToFetch), new Function<Publication, Long>() {
            @Override
            public Long apply(Publication publication) {
                Preconditions.checkNotNull(publication);
                return publication.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                publications.add(id2Map.get(id));
            }
        }
        return new PublicationPage(publications, new PageRequest(start, size),
                response.getHits().getTotalHits());
    }

    @Override
    public Publication findOnePublication(Long id) {
        return publicationRepository.findOne(id);
    }

    @Override
    public Set<Experiment> findExperimentsByPublication(Long id) {
        Publication publication = publicationRepository.findOne(id);
        // INFO neccessary because umodifiable list
        Set<Experiment> experiments = Sets.newCopyOnWriteArraySet(publication.getExperiments());
        return experiments;
    }

    private long countAnalysesByExperiment(Long experimentId) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        FilterBuilder filter = FilterBuilders.boolFilter().must(FilterBuilders.hasParentFilter("phenotype", FilterBuilders.termFilter("_parent", experimentId.toString())), esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")));
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(filter);
        SearchResponse response = request.setTypes("study").setSize(0).setQuery(query).execute().actionGet();
        return response.getHits().getTotalHits();
    }

    private List<ESFacet> getPhenotypeStats(Long experimentId) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex()).setTypes("phenotype").setSize(0);
        FilterBuilder filter = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("_parent", experimentId.toString()), esAclManager.getAclFilterForPermissions(Lists.newArrayList("read")));
        //TODO change mapping so that term_name is not analyzed
        request.setQuery(QueryBuilders.constantScoreQuery(filter))
                .addAggregation(AggregationBuilders.terms("TO").field("to_accession.term_name.raw"))
                .addAggregation(AggregationBuilders.terms("EO").field("eo_accession.term_name.raw"));
        SearchResponse response = request.execute().actionGet();
        List<ESFacet> facets = Lists.newArrayList();
        Terms searchFacet = response.getAggregations().get("TO");
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (Terms.Bucket bucket : searchFacet.getBuckets()) {
            terms.add(new ESTermsFacet(bucket.getKey(), bucket.getDocCount()));
        }
        facets.add(new ESFacet("TO", 0, terms.size(), 0, terms));
        // TO
        searchFacet = response.getAggregations().get("EO");
        terms = Lists.newArrayList();
        for (Terms.Bucket bucket : searchFacet.getBuckets()) {
            terms.add(new ESTermsFacet(bucket.getKey(), bucket.getDocCount()));
        }
        facets.add(new ESFacet("EO", 0, terms.size(), 0, terms));
        return facets;
    }
}
