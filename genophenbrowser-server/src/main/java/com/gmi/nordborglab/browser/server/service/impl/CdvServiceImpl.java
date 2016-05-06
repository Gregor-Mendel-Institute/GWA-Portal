package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.controller.rest.exceptions.ResourceNotFoundException;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.domain.DomainFunctions;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.es.EsIndexer;
import com.gmi.nordborglab.browser.server.es.EsSearcher;
import com.gmi.nordborglab.browser.server.exceptions.CommandLineException;
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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
    private EsIndexer esIndexer;

    @Resource
    private EsSearcher esSearcher;

    @Resource
    private EsAclManager esAclManager;

    @Resource
    private MetaAnalysisService cdvService;

    @Value("${AMQP.RUN_BY_DEFAULT_ON_HPC}")
    private Boolean RUN_BY_DEFAULT_ON_HPC;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CdvService.class);

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
        if (study == null)
            throw new ResourceNotFoundException("Analysis not found");
        study = helperService.applyTransformation(study);
        study = aclManager.setPermissionAndOwner(study);
        return study;
    }


    @Override
    @Transactional(readOnly = false, noRollbackFor = CommandLineException.class)
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
            if (study.getJob() != null) {
                if (RUN_BY_DEFAULT_ON_HPC) {
                    study.getJob().setHPC(true);
                }
            }
        }
        if (study.getPseudoHeritability() == null) {
            try {
                study.setPseudoHeritability(helperService.getPseudoHeritability(study));
            } catch (CommandLineException e) {
                logger.error("Failed to calculate pseudo_heritability", e);
            }
        }
        if (study.getShapiroWilkPvalue() == null) {
            try {
                study.setShapiroWilkPvalue(helperService.calculateShapiroWilkPvalue(study));
            } catch (Exception e) {
                logger.error("Failed to calculate shapiro wilk pvalue", e);
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
                cdvService.createCandidateGeneListEnrichments(study.getId(), ConstEnums.ENRICHMENT_TYPE.ANALYSIS, true, null);
            }
        }
        study = aclManager.setPermissionAndOwner(study);
        indexStudy(study);
        return study;
    }

    private void indexStudy(Study study) {
        try {
            esIndexer.index(study);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        SearchResponse response = esSearcher.search(filter, phenotypeId, TraitUom.ES_TYPE, false, new String[]{"name^3.5", "name.partial^1.5", "protocol.analysis_method^3.5", "genotype.name^1.5", "genotype.producer", "owner.name", "experiment.name", "phenotype.name"}, searchString, Study.ES_TYPE, start, size);
        List<Long> idsToFetch = EsSearcher.getIdsFromResponse(response);
        List<Study> resultsFromDb = studyRepository.findAll(idsToFetch);
        //extract facets
        List<ESFacet> facets = EsSearcher.getAggregations(response);
        Ordering<Study> orderByEs = Ordering.explicit(idsToFetch).onResultOf(DomainFunctions.getStudyId());
        List<Study> results = orderByEs.immutableSortedCopy(resultsFromDb);
        aclManager.setPermissionAndOwners(results);
        return new StudyPage(results, new PageRequest(start, size), response.getHits().getTotalHits(), facets);
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
        if (RUN_BY_DEFAULT_ON_HPC) {
            job.setHPC(true);
        }

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
        deleteCandidatGeneEnrichmentFromIndex(study.getId());
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
        deleteCandidatGeneEnrichmentFromIndex(study.getId());
        deleteFromIndex(study);
    }


    private void deleteMetaAnalysisFromIndex(Long studyId) {
        new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
                .setIndices(esAclManager.getIndex())
                .setTypes("meta_analysis_snps")
                .setQuery(QueryBuilders.termQuery("studyid", studyId)).execute();
    }

    private void deleteFromIndex(Study study) {
        esIndexer.delete(study);
    }

    private void deleteCandidatGeneEnrichmentFromIndex(Long studyId) {
        new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE).setIndices(esAclManager.getIndex()).setTypes("candidate_gene_list_enrichment").setQuery(QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("study_.id", studyId))).execute();
    }
}
