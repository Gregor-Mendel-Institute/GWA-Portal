package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.repository.AlleleAssayRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.CustomUser;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.filter.FilterFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

@Service
@Transactional(readOnly = true)
public class CdvServiceImpl implements CdvService {

    @Resource
    protected UserRepository userRepository;

    @Resource
    protected StudyRepository studyRepository;

    @Resource
    protected TraitUomRepository traitUomRepository;

    @Resource
    protected TraitRepository traitRepository;

    @Resource
    protected RoleHierarchy roleHierarchy;

    @Resource
    protected AlleleAssayRepository alleleAssayRepository;

    @Resource
    protected AclManager aclManager;

    @Resource
    protected GWASDataService gwasDataService;

    @Resource
    protected HelperService helperService;

    @Resource
    private Client client;

    @Resource
    private EsAclManager esAclManager;

    @Resource
    private MetaAnalysisService cdvService;


    //TODO change ACL
    @Override
    public StudyPage findStudiesByPhenotypeId(Long id, int start, int size) {
        StudyPage page = null;
        PageRequest pageRequest = new PageRequest(start, size);
        FluentIterable<Study> studies = aclManager.filterByAcl(studyRepository.findByPhenotypeId(id));
        int totalElements = studies.size();
        int pageStart = 0;
        if (start > 0)
            pageStart = start / size;
        if (totalElements > 0) {
            List<Study> partitionedStudies = Iterables.get(Iterables.partition(studies, size), pageStart);
            page = new StudyPage(partitionedStudies, pageRequest,
                    totalElements, null);
        } else {
            page = new StudyPage(studies.toList().asList(), pageRequest, 0, null);
        }
        return page;
    }


    @Override
    public Study findStudy(Long id) {
        Study study = studyRepository.findOne(id);
        study = helperService.applyTransformation(study);
        study = aclManager.setPermissionAndOwner(study);
        return study;
    }


    @Override
    @Transactional(readOnly = false)
    public Study saveStudy(Study study) {
        boolean isNewRecord = study.getId() == null;
        CustomUser user = SecurityUtil.getUserFromContext();
        if (user != null)
            study.setProducer(user.getFullName());
        if (study.getJob() != null && study.getJob().getAppUser() == null) {
            AppUser appUser = userRepository.findOne(Long.parseLong(SecurityUtil.getUsername()));
            study.getJob().setAppUser(appUser);
        }

        // check if alleleAssay can be used
        if (isNewRecord) {
            if (!aclManager.hasPermission(SecurityUtil.getAuthentication(), study.getAlleleAssay(), CustomPermission.USE)) {
                throw new AccessDeniedException("No access");
            }
        }
        study = studyRepository.save(study);
        Long traitUomId = Iterables.get(study.getTraits(), 0).getTraitUom().getId();
        if (isNewRecord) {
            CumulativePermission permission = new CumulativePermission();
            permission.set(CustomPermission.ADMINISTRATION);
            permission.set(CustomPermission.EDIT);
            permission.set(CustomPermission.READ);
            aclManager.addPermission(study, new PrincipalSid(SecurityUtil.getUsername()),
                    permission, TraitUom.class, traitUomId);
            aclManager.addPermission(study, new GrantedAuthoritySid("ROLE_ADMIN"), permission, TraitUom.class, traitUomId);
            if (study.isCreateEnrichments()) {
                cdvService.createCandidateGeneListEnrichments(study, true, null);
            }
        }
        study = aclManager.setPermissionAndOwner(study);
        indexStudy(study);
        return study;
    }

    private void indexStudy(Study study) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();

            builder.startObject()
                    .field("name", study.getName())
                    .field("published", study.getPublished())
                    .field("modified", study.getModified())
                    .field("created", study.getCreated())
                    .field("producer", study.getProducer())
                    .field("study_date", study.getStudyDate())
                    .startObject("phenotype")
                    .field("name", study.getPhenotype().getLocalTraitName())
                    .field("id", study.getPhenotype().getId())
                    .endObject()
                    .startObject("experiment")
                    .field("name", study.getPhenotype().getExperiment().getName())
                    .field("id", study.getPhenotype().getExperiment().getId())
                    .endObject();

            if (study.getProtocol() != null) {
                builder.startObject("protocol")
                        .field("analysis_method", study.getProtocol().getAnalysisMethod()).endObject();
            }

            if (study.getAlleleAssay() != null) {
                getAlleleAssayBuilder(builder, study.getAlleleAssay());
            }
            //TODO do the same thing for Environment ontology

            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(study));
            builder.endObject();
            IndexRequestBuilder request = client.prepareIndex(esAclManager.getIndex(), "study", study.getId().toString())
                    .setSource(builder).setParent(study.getPhenotype().getId().toString()).setRouting(study.getPhenotype().getExperiment().getId().toString());
            IndexResponse response = request.execute().actionGet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XContentBuilder getAlleleAssayBuilder(XContentBuilder builder, AlleleAssay alleleAssay) {
        try {
            builder.startObject("allele_assay")
                    .field("assay_date", alleleAssay.getAssayDate())
                    .field("name", alleleAssay.getName())
                    .field("producer", alleleAssay.getProducer())
                    .field("comments", alleleAssay.getComments())
                    .startObject("scoring_tech_type")
                    .field("scoring_tech_group", alleleAssay.getScoringTechType().getScoringTechGroup())
                    .field("scoring_tech_type", alleleAssay.getScoringTechType().getScoringTechType())
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return builder;
    }


    @Override
    public List<Study> findStudiesByPassportId(Long passportId) {
        Sort sort = new Sort("id");
        return studyRepository.findAllByPassportId(passportId, sort);
    }


    @Override
    public StudyPage findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        return findAll(null, filter, searchString, start, size);
    }

    @Override
    public StudyPage findAll(Long phenotypeId, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        FilterBuilder phenotypeFilter = null;
        if (phenotypeId != null) {
            phenotypeFilter = FilterBuilders.termFilter("_parent", phenotypeId.toString());
        }

        SearchRequestBuilder request = client.prepareSearch(esAclManager.getIndex());
        request.setSize(size).setFrom(start).setTypes("study").setNoFields();

        if (searchString != null && !searchString.equalsIgnoreCase("")) {
            request.setQuery(multiMatchQuery(searchString, "name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "allele_assay.name^1.5", "allele_assay.producer", "owner.name", "experiment.name", "phenotype.name"));
        }
        FilterBuilder searchFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, false);
        FilterBuilder privateFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), true, false);
        FilterBuilder publicFilter = esAclManager.getAclFilter(Lists.newArrayList("read"), false, true);

        if (phenotypeFilter != null) {
            searchFilter = FilterBuilders.boolFilter().must(phenotypeFilter, searchFilter);
            privateFilter = FilterBuilders.boolFilter().must(phenotypeFilter, searchFilter);
            publicFilter = FilterBuilders.boolFilter().must(phenotypeFilter, searchFilter);
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
                    request.addSort("name", SortOrder.ASC);
        }
        // set filter
        request.setPostFilter(searchFilter);

        SearchResponse response = request.execute().actionGet();
        List<Long> idsToFetch = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            idsToFetch.add(Long.parseLong(hit.getId()));
        }
        List<Study> studies = Lists.newArrayList();
        //Neded because ids are not sorted

        Map<Long, Study> id2Map = Maps.uniqueIndex(studyRepository.findAll(idsToFetch), new Function<Study, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Study study) {
                return study.getId();
            }
        });
        for (Long id : idsToFetch) {
            if (id2Map.containsKey(id)) {
                studies.add(id2Map.get(id));
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

        aclManager.setPermissionAndOwners(studies);
        return new StudyPage(studies, new PageRequest(start, size), response.getHits().getTotalHits(), facets);
    }

    @Override
    public List<Trait> findTraitValues(Long studyId) {
        Study study = studyRepository.findOne(studyId);
        List<Trait> traits = traitRepository.findAllByStudiesId(studyId);
        return traits;
    }

    @Override
    public List<AlleleAssay> findAlleleAssaysWithStats(Long phenotypeId, Long statisticTypeId) {
        Long traitValuesCount = traitRepository.countNumberOfTraitValues(phenotypeId, statisticTypeId);
        List<AlleleAssay> alleleAssays = alleleAssayRepository.findAll();
        for (AlleleAssay alleleAssay : alleleAssays) {
            Long availableAllelesCount = alleleAssayRepository.countAvailableAlleles(phenotypeId, statisticTypeId, alleleAssay.getId());
            alleleAssay.setTraitValuesCount(traitValuesCount);
            alleleAssay.setAvailableAllelesCount(availableAllelesCount);
        }
        return alleleAssays;
    }

    @Transactional(readOnly = false)
    @Override
    public Study createStudyJob(Long studyId) {
        Study study = studyRepository.findOne(studyId);
        if (study.getJob() != null) {
            throw new RuntimeException("Study has already a job assigned");
        }
        study = aclManager.setPermissionAndOwner(study);
        StudyJob job = new StudyJob();
        job.setStatus("Waiting");
        job.setProgress(1);
        job.setCreateDate(new Date());
        job.setModificationDate(new Date());
        job.setTask("Waiting for workflow to start");
        AppUser appUser = userRepository.findOne(Long.parseLong(SecurityUtil.getUsername()));
        job.setAppUser(appUser);
        study.setJob(job);
        studyRepository.save(study);
        return study;
    }

    @Transactional(readOnly = false)
    @Override
    public Study deleteStudyJob(Long studyId) {
        Study study = studyRepository.findOne(studyId);
        study = aclManager.setPermissionAndOwner(study);
        if (study.getJob() == null) {
            return study;
        }
        study.removeJob();
        studyRepository.save(study);
        gwasDataService.deleteStudyFile(studyId);
        deleteMetaAnalysisFromIndex(studyId);
        return study;
    }

    @Transactional(readOnly = false)
    @Override
    public Study rerunAnalysis(Long studyId) {
        Study study = studyRepository.findOne(studyId);
        study = aclManager.setPermissionAndOwner(study);
        if (study.getJob() == null || !study.getJob().getStatus().equalsIgnoreCase("Error")) {
            return study;
        }
        study.getJob().setProgress(1);
        study.getJob().setStatus("Waiting");
        study.getJob().setModificationDate(new Date());
        study.getJob().setTask("Waiting for workflow to start");
        study.getJob().setPayload(null);
        studyRepository.save(study);
        return study;
    }


    @Transactional(readOnly = false)
    @Override
    public void delete(Study study) {
        aclManager.setPermissionAndOwner(study);
        if (study.isPublic()) {
            throw new RuntimeException("Public analysis can't be deleted");
        }
        Long studyId = study.getId();
        Long experimentId = study.getPhenotype().getExperiment().getId();
        // necessary otherwise foreign key error
        for (Trait traits : study.getTraits()) {
            traits.getStudies().remove(study);
        }
        for (CandidateGeneListEnrichment enrichment : study.getCandidateGeneListEnrichments()) {
            enrichment.delete();
        }
        gwasDataService.deleteStudyFile(studyId);
        studyRepository.delete(study);
        aclManager.deletePermissions(study, true);
        //throw new RuntimeException("Runtimeexception");
        deleteMetaAnalysisFromIndex(studyId);
        deleteFromIndex(studyId, experimentId);
    }


    private void deleteMetaAnalysisFromIndex(Long studyId) {
        DeleteByQueryRequestBuilder request = client.prepareDeleteByQuery(esAclManager.getIndex())
                .setTypes("meta_analysis_snps")
                .setQuery(QueryBuilders.termQuery("studyid", studyId));
        request.execute();
    }

    private void deleteFromIndex(Long studyId, Long experimentId) {
        client.prepareDelete(esAclManager.getIndex(), "study", studyId.toString()).setRouting(experimentId.toString()).execute();
        deleteCandidatGeneEnrichmentFromIndex(studyId);
    }

    private void deleteCandidatGeneEnrichmentFromIndex(Long studyId) {
        try {
            QueryBuilder query = QueryBuilders.constantScoreQuery(FilterBuilders.termFilter("study_.id", studyId));
            client.prepareDeleteByQuery(esAclManager.getIndex()).setTypes("candidate_gene_list_enrichment").setQuery(query).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
