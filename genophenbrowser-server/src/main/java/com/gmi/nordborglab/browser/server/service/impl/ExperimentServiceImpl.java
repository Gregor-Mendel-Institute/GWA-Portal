package com.gmi.nordborglab.browser.server.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.Valid;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.es.ESTermsFacet;
import com.gmi.nordborglab.browser.server.domain.pages.PublicationPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.repository.PublicationRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.search.PublicationSearchProcessor;
import com.gmi.nordborglab.browser.server.security.*;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.query.parser.QueryBuilder;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentFactory.*;
import org.elasticsearch.index.query.ConstantScoreQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.filter.FilterFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import sun.nio.cs.Surrogate;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
@Validated
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
    private EsAclManager esAclManager;

    @Resource
    private TermRepository termRepository;

    @Resource
    PermissionFactory permissionFactory;


    @Transactional(readOnly = false)
    @Override
    public Experiment save(@Valid Experiment experiment) {
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
        Long experimentId = experiment.getId();
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
        deleteFromIndex(experimentId);
    }

    private void deleteFromIndex(Long experimentId) {
        client.prepareDelete(esAclManager.getIndex(), "experiment", experimentId.toString()).setRouting(experimentId.toString()).execute();
    }

    private void indexExperiment(Experiment experiment) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();

            builder.startObject()
                    .field("id", experiment.getId().toString())
                    .field("name", experiment.getName())
                    .field("published", experiment.getPublished())
                    .field("originator", experiment.getOriginator())
                    .field("comments", experiment.getComments())
                    .field("modified", experiment.getModified())
                    .field("created", experiment.getCreated());
            if (experiment.getPublications() != null && experiment.getPublications().size() > 0) {
                builder.startArray("publication");
                for (Publication publication : experiment.getPublications()) {
                    getPublicationIndexBuilder(builder, publication);
                }
                builder.endArray();
            }
            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(experiment));
            builder.endObject();
            String test = builder.string();

            IndexRequestBuilder request = client.prepareIndex(esAclManager.getIndex(), "experiment", experiment.getId().toString()).setRouting(experiment.getId().toString())
                    .setSource(builder);

            request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private XContentBuilder getPublicationIndexBuilder(XContentBuilder builder, Publication publication) {
        try {
            builder.startObject()
                    .field("journal", publication.getJournal())
                    .field("author", publication.getFirstAuthor())
                    .field("title", publication.getTitle())
                    .field("page", publication.getPage())
                    .field("pubdate", publication.getPubDate())
                    .field("issue", publication.getIssue())
                    .field("volune", publication.getVolume())
                    .field("url", publication.getURL())
                    .field("doi", publication.getURL())
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return builder;
    }


    @Override
    public ExperimentPage findByAclAndFilter(ConstEnums.TABLE_FILTER filter, String searchString, int page, int size) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(page).setTypes("experiment").setNoFields();

        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            request.setQuery(multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "originator", "design", "comments^0.5"));
        }
        FilterBuilder searchFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, false);
        FilterBuilder privateFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), true, false);
        FilterBuilder publicFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, true);

        // set facets
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.TABLE_FILTER.ALL.name()).filter(searchFilter));
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.TABLE_FILTER.PRIVATE.name()).filter(privateFilter));
        request.addFacet(FacetBuilders.filterFacet(ConstEnums.TABLE_FILTER.PUBLISHED.name()).filter(publicFilter));

        switch (filter) {
            case PRIVATE:
                searchFilter = privateFilter;
                break;
            case PUBLISHED:
                searchFilter = publicFilter;
                break;
            case RECENT:
                request.addSort("modified", SortOrder.DESC);
                break;
            default:
                if (searchString == null || searchString.isEmpty())
                    request.addSort("name", SortOrder.ASC);
        }
        // set filter
        request.setFilter(searchFilter);

        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<Experiment> experiments = Lists.newArrayList();
        //Neded because ids are not sorted
        Map<Long, Experiment> id2Map = Maps.uniqueIndex(experimentRepository.findAll(idsToFetch), new Function<Experiment, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Experiment experiment) {
                return experiment.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                experiments.add(id2Map.get(id));
            }
        }

        //extract facets
        Facets searchFacets = response.getFacets();
        List<ESFacet> facets = Lists.newArrayList();

        FilterFacet filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.ALL.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.ALL.name(), 0, filterFacet.getCount(), 0, null));
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.RECENT.name(), 0, filterFacet.getCount(), 0, null));

        filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.PRIVATE.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.PRIVATE.name(), 0, filterFacet.getCount(), 0, null));

        // get annotation
        filterFacet = (FilterFacet) searchFacets.facetsAsMap().get(ConstEnums.TABLE_FILTER.PUBLISHED.name());
        facets.add(new ESFacet(ConstEnums.TABLE_FILTER.PUBLISHED.name(), 0, filterFacet.getCount(), 0, null));

        aclManager.setPermissionAndOwners(experiments);
        return new ExperimentPage(experiments, new PageRequest(page, size), response.getHits().getTotalHits(), facets);
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
            @Nullable
            @Override
            public Long apply(@Nullable Publication publication) {
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
        FilterBuilder filter = FilterBuilders.boolFilter().must(FilterBuilders.hasParentFilter("phenotype", FilterBuilders.termFilter("_parent", experimentId.toString())), esAclManager.getAclFilter(Lists.newArrayList("read")));
        ConstantScoreQueryBuilder query = QueryBuilders.constantScoreQuery(filter);
        SearchResponse response = request.setTypes("study").setSize(0).setQuery(query).execute().actionGet();
        return response.getHits().getTotalHits();
    }

    private List<ESFacet> getPhenotypeStats(Long experimentId) {
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex()).setTypes("phenotype").setSize(0);
        FilterBuilder filter = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("_parent", experimentId.toString()), esAclManager.getAclFilter(Lists.newArrayList("read")));
        //TODO change mapping so that term_name is not analyzed
        request.setQuery(QueryBuilders.constantScoreQuery(filter))
                .addFacet(FacetBuilders.termsFacet("TO").field("to_accession.term_name.raw"))
                .addFacet(FacetBuilders.termsFacet("EO").field("eo_accession.term_name.raw"));
        SearchResponse response = request.execute().actionGet();
        List<ESFacet> facets = Lists.newArrayList();
        TermsFacet searchFacet = (TermsFacet) response.getFacets().facetsAsMap().get("TO");
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            terms.add(new ESTermsFacet(termEntry.getTerm().string(), termEntry.getCount()));
        }
        facets.add(new ESFacet("TO", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));
        // TO
        searchFacet = (TermsFacet) response.getFacets().facetsAsMap().get("EO");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            terms.add(new ESTermsFacet(termEntry.getTerm().string(), termEntry.getCount()));
        }
        facets.add(new ESFacet("EO", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));
        return facets;
    }
}
