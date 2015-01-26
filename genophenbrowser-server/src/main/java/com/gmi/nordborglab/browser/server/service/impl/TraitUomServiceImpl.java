package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.StatisticTypeRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.filter.FilterFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
@Transactional(readOnly = true)
public class TraitUomServiceImpl extends WebApplicationObjectSupport implements TraitUomService {


    @Resource
    private HelperService helperService;

    @Resource
    private TraitUomRepository traitUomRepository;

    @Resource
    private CdvService cdvService;

    @Resource
    private StudyRepository studyRepository;

    @Resource
    private TermRepository termRepository;

    @Resource
    private RoleHierarchy roleHierarchy;

    @Resource
    private AclManager aclManager;

    @Resource
    private Client client;

    @Resource
    private EsAclManager esAclManager;

    @Resource
    private StatisticTypeRepository statisticTypeRepository;

    @Resource
    private PassportRepository passportRepository;

    @Override
    public TraitUomPage findPhenotypesByExperiment(Long id, int start, int size) {
        TraitUomPage page = null;
        PageRequest pageRequest = new PageRequest(start, size);
        FluentIterable<TraitUom> traits = findPhenotypesByExperimentid(id);
        int totalElements = traits.size();
        int pageStart = 0;
        if (start > 0)
            pageStart = start / size;
        if (totalElements > 0) {
            List<TraitUom> partitionedTraits = Iterables.get(Iterables.partition(traits, size), pageStart);
            for (TraitUom trait : partitionedTraits) {
                initOntologies(trait);
            }
            page = new TraitUomPage(partitionedTraits, pageRequest,
                    totalElements, null);
        } else {
            page = new TraitUomPage(traits.toList().asList(), pageRequest, 0, null);
        }
        return page;
    }

    private FluentIterable<TraitUom> findPhenotypesByExperimentid(Long id) {

        return aclManager.filterByAcl(traitUomRepository.findByExperimentId(id));
    }


    ///TODO Custom query for better performance
    @Override
    public int countPhenotypeByExperimentCount(Long id) {
        FluentIterable<TraitUom> traits = findPhenotypesByExperimentid(id);
        return traits.size();
    }

    @Override
    public TraitUom findPhenotype(Long id) {
        TraitUom traitUom = traitUomRepository.findOne(id);
        List<StatisticType> statisticTypeToReturn = Lists.newArrayList();
        List<Object[]> statisticTypes = traitUomRepository.countTraitsForStatisticType(id);
        for (Object[] statisticTypeWithCount : statisticTypes) {
            StatisticType type = (StatisticType) statisticTypeWithCount[0];
            type.setNumberOfTraits((Long) statisticTypeWithCount[1]);
            statisticTypeToReturn.add(type);
        }
        traitUom.setStatisticTypes(statisticTypeToReturn);
        traitUom = setStats(traitUom);
        traitUom = aclManager.setPermissionAndOwner(traitUom);
        initOntologies(traitUom);
        return traitUom;
    }

    @Override
    @Transactional(readOnly = false)
    public TraitUom save(TraitUom traitUom) {
        if (traitUom.getId() == null)
            throw new RuntimeException("use create method for adding new traits");
        traitUom = traitUomRepository.save(traitUom);
        traitUom = setStats(traitUom);
        traitUom = aclManager.setPermissionAndOwner(traitUom);
        initOntologies(traitUom);
        indexTraitUom(traitUom);
        return traitUom;
    }

    @Transactional(readOnly = false)
    @Override
    public void delete(TraitUom traitUom) {
        aclManager.setPermissionAndOwner(traitUom);
        Long phenotypeId = traitUom.getId();
        Long experimentId = traitUom.getExperiment().getId();
        if (traitUom.isPublic()) {
            throw new RuntimeException("Public phenotypes can't be deleted");
        }
        List<Study> studies = studyRepository.findByPhenotypeId(traitUom.getId());
        for (Study study : studies) {
            cdvService.delete(study);
        }
        for (Trait trait : traitUom.getTraits()) {
            trait.setTraitUom(null);
        }
        traitUom.getTraits().clear();
        traitUomRepository.delete(traitUom);
        aclManager.deletePermissions(traitUom, true);
        deleteFromIndex(phenotypeId, experimentId);
    }

    private void deleteFromIndex(Long phenotypeId, Long experimentId) {
        client.prepareDelete(esAclManager.getIndex(), "phenotype", phenotypeId.toString()).setRouting(experimentId.toString()).execute();
    }


    private TraitUom setStats(TraitUom traitUom) {
        traitUom.setNumberOfObsUnits(traitUomRepository.countObsUnitsByPhenotypeId(traitUom.getId()));
        traitUom.setNumberOfStudies(traitUomRepository.countStudiesByPhenotypeId(traitUom.getId()));
        return traitUom;
    }

    @Override
    public List<TraitUom> findPhenotypesByPassportId(Long passportId) {
        Sort sort = new Sort("id");
        return aclManager.filterByAcl(traitUomRepository.findAllByPasportId(passportId, sort)).toList();
    }

    @Override
    public TraitUomPage findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        return findAll(null, filter, searchString, start, size);
    }

    @Override
    public TraitUomPage findAll(Long experimentId, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        FilterBuilder experimentFilter = null;
        if (experimentId != null) {
            experimentFilter = FilterBuilders.termFilter("_parent", experimentId.toString());
        }
        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes("phenotype").setNoFields();

        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            request.setQuery(multiMatchQuery(searchString, "local_trait_name^3.5", "local_trait_name.partial^1.5", "trait_protocol", "to_accession.term_id^3.5", "to_accession.term_name^1.5", "eo_accession.term_id^3.5", "eo_accession.term_name^1.5", "owner.name", "experiment.name"));
        }
        FilterBuilder searchFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, false);
        FilterBuilder privateFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), true, false);
        FilterBuilder publicFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, true);
        if (experimentFilter != null) {
            searchFilter = FilterBuilders.boolFilter().must(experimentFilter, searchFilter);
            privateFilter = FilterBuilders.boolFilter().must(experimentFilter, searchFilter);
            publicFilter = FilterBuilders.boolFilter().must(experimentFilter, searchFilter);
        }

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
                    request.addSort("local_trait_name", SortOrder.ASC);
        }
        // set filter
        request.setPostFilter(searchFilter);

        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<TraitUom> traits = Lists.newArrayList();
        //Neded because ids are not sorted

        Map<Long, TraitUom> id2Map = Maps.uniqueIndex(traitUomRepository.findAll(idsToFetch), new Function<TraitUom, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable TraitUom trait) {
                return trait.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                traits.add(id2Map.get(id));
            }
        }
        for (TraitUom traitUom : traits) {
            initOntologies(traitUom);

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

        aclManager.setPermissionAndOwners(traits);
        return new TraitUomPage(traits, new PageRequest(start, size), response.getHits().getTotalHits(), facets);
    }

    @Override
    public List<TraitUom> findPhenotypesByExperimentAndAcl(Long id, int permission) {
        List<TraitUom> traitsToReturn = Lists.newArrayList();
        final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        //TODO either change signature to always use WRITE permission or retrieve correct permission from int
        final ImmutableList<Permission> permissions = ImmutableList.of(CustomPermission.EDIT);
        List<TraitUom> traits = traitUomRepository.findByExperimentId(id);
        if (traits.size() > 0) {
            FluentIterable<TraitUom> traitsToFilter = aclManager.filterByAcl(traits, permissions);
            traitsToReturn = traitsToFilter.toList();
            for (TraitUom traitUom : traitsToReturn) {
                //FIXME because Hibernate returns singleton instances setting a transient field like numberofTraits will overwrite the same field for other phenotypes.
                List<Long> statisticTypeTraitCounts = Lists.newArrayList();
                List<StatisticType> statisticTypesToReturn = Lists.newArrayList();
                List<Object[]> statisticTypes = traitUomRepository.countTraitsForStatisticType(traitUom.getId());
                for (Object[] statisticTypeWithCount : statisticTypes) {
                    StatisticType type = (StatisticType) statisticTypeWithCount[0];
                    type.setNumberOfTraits((Long) statisticTypeWithCount[1]);
                    statisticTypesToReturn.add(type);
                    statisticTypeTraitCounts.add(type.getNumberOfTraits());
                }
                traitUom.setStatisticTypes(statisticTypesToReturn);
                traitUom.setStatisticTypeTraitCounts(statisticTypeTraitCounts);
                traitUom = setStats(traitUom);
                traitUom = aclManager.setPermissionAndOwner(traitUom);
                initOntologies(traitUom);
            }
        }
        return traitsToReturn;
    }

    @Transactional(readOnly = false)
    @Override
    public List<TraitUom> savePhenotypeUploadData(Experiment experiment, List<PhenotypeUploadData> data, List<SampleData> samples) {
        checkNotNull(experiment);
        checkNotNull(samples);
        checkNotNull(data);
        checkArgument(experiment.getId() != null);
        StatisticType statisticType = statisticTypeRepository.findOne(1L);
        checkNotNull(statisticType);
        for (SampleData sample : samples) {
            if (sample.hasError()) {
                continue;
            }
            Passport passport = passportRepository.findOne(sample.getPassportId());
            Stock stock = passport.getStocks().get(0);
            ObsUnit obsUnit = new ObsUnit();
            obsUnit.setStock(stock);
            obsUnit.setExperiment(experiment);
            for (int i = 0; i < data.size(); i++) {
                String phenotypeValue = sample.getValues().get(i);
                if (phenotypeValue == null || phenotypeValue.isEmpty())
                    continue;
                TraitUom traitUom = data.get(i).getTraitUom();
                Trait trait = new Trait();
                trait.setStatisticType(statisticType);
                trait.setValue(phenotypeValue);
                trait.setObsUnit(obsUnit);
                traitUom.addTrait(trait);
            }
        }
        for (PhenotypeUploadData phenotypeUploadData : data) {
            TraitUom traitUom = phenotypeUploadData.getTraitUom();
            traitUomRepository.save(traitUom);
            initOntologies(traitUom);
            CumulativePermission permission = new CumulativePermission();
            permission.set(CustomPermission.ADMINISTRATION);
            permission.set(CustomPermission.EDIT);
            permission.set(CustomPermission.READ);
            aclManager.addPermission(traitUom, new PrincipalSid(SecurityUtil.getUsername()),
                    permission, Experiment.class, experiment.getId());
            aclManager.addPermission(traitUom, new GrantedAuthoritySid("ROLE_ADMIN"), permission, Experiment.class, experiment.getId());
            indexTraitUom(traitUom);
        }
        return Lists.newArrayList(Iterables.transform(data, new Function<PhenotypeUploadData, TraitUom>() {
            @Nullable
            @Override
            public TraitUom apply(@Nullable PhenotypeUploadData input) {
                return input.getTraitUom();
            }
        }));
    }


    private void indexTraitUom(TraitUom traitUom) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();

            builder.startObject()
                    .field("local_trait_name", traitUom.getLocalTraitName())
                    .field("published", traitUom.getPublished())
                    .field("modified", traitUom.getModified())
                    .field("created", traitUom.getCreated())
                    .field("trait_protocol", traitUom.getTraitProtocol())
                    .startObject("experiment")
                    .field("name", traitUom.getExperiment().getName())
                    .field("id", traitUom.getExperiment().getId())
                    .endObject();
            if (traitUom.getTraitOntologyTerm() != null) {
                builder.startObject("to_accession");
                getOntologyBuilder(builder, traitUom.getTraitOntologyTerm());
                builder.endObject();
            }
            if (traitUom.getEnvironOntologyTerm() != null) {
                builder.startObject("eo_accession");
                getOntologyBuilder(builder, traitUom.getEnvironOntologyTerm());
                builder.endObject();
            }
            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(traitUom));
            builder.endObject();
            IndexRequestBuilder request = client.prepareIndex(esAclManager.getIndex(), "phenotype", traitUom.getId().toString()).setRouting(traitUom.getExperiment().getId().toString())
                    .setSource(builder).setParent(traitUom.getExperiment().getId().toString());

            request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XContentBuilder getOntologyBuilder(XContentBuilder builder, Term term) {
        try {
            builder.field("term_id", term.getAcc())
                    .field("term_definition", term.getTermDefinition().getTermDefinition())
                    .field("term_comment", term.getTermDefinition().getTermComment())
                    .field("term_name", term.getName());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return builder;
    }

    @Override
    public List<TraitUom> findAllByOntology(String type, String acc, boolean checkChilds) {
        List<TraitUom> traits = null;
        List<String> ontologyTerms = getOntologyList(acc, checkChilds);
        if ("trait".equalsIgnoreCase(type)) {
            traits = traitUomRepository.findAllByToAccessionIn(ontologyTerms);
        } else if ("environment".equalsIgnoreCase(type)) {
            traits = traitUomRepository.findAllByEoAccessionIn(ontologyTerms);
        } else {
            throw new RuntimeException(type + " Type unknown");
        }
        for (TraitUom trait : traits) {
            initOntologies(trait);
        }
        return traits;
    }


    private List<String> getOntologyList(String acc, boolean checkChilds) {
        List<String> list = Lists.newArrayList(acc);
        if (checkChilds) {
            Term term = termRepository.findByAcc(acc);
            addChildOntologies(term, list);
        }
        return list;
    }

    private void addChildOntologies(Term term, List<String> list) {
        if (term == null) {
            return;
        }
        list.add(term.getAcc());
        for (Term2Term term2Term : term.getChilds()) {
            addChildOntologies(term2Term.getChild(), list);
        }
    }

    private void initOntologies(TraitUom traitUom) {
        if (traitUom.getToAccession() != null) {
            traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
        }
        if (traitUom.getEoAccession() != null) {
            traitUom.setEnvironOntologyTerm(termRepository.findByAcc(traitUom.getEoAccession()));
        }
    }
}

