package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.repository.*;
import com.gmi.nordborglab.browser.server.security.*;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
                    totalElements);
        } else {
            page = new StudyPage(studies.toList().asList(), pageRequest, 0);
        }
        return page;
    }


    @Override
    public Study findStudy(Long id) {
        Study study = studyRepository.findOne(id);
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
        study = studyRepository.save(study);
        Long traitUomId = Iterables.get(study.getTraits(), 0).getTraitUom().getId();
        if (isNewRecord) {
            CumulativePermission permission = new CumulativePermission();
            permission.set(CustomPermission.ADMINISTRATION);
            permission.set(CustomPermission.EDIT);
            permission.set(CustomPermission.READ);
            aclManager.addPermission(study, new PrincipalSid(SecurityUtil.getUsername()),
                    permission, traitUomId);
            aclManager.addPermission(study, new GrantedAuthoritySid("ROLE_ADMIN"), permission, traitUomId);
        }
        study = aclManager.setPermissionAndOwner(study);
        return study;
    }


    @Override
    public List<Study> findStudiesByPassportId(Long passportId) {
        Sort sort = new Sort("id");
        return studyRepository.findAllByPassportId(passportId, sort);
    }


    @Override
    public StudyPage findAll(String name, String phenotype, String experiment,
                             Long alleleAssayId, Long studyProtocolId, int start, int size) {
        StudyPage page;
        int pageStart = 0;
        if (start > 0)
            pageStart = start / size;
        PageRequest pageRequest = new PageRequest(start, size);
        Sort sort = new Sort("id");
        ImmutableList<Study> studies = aclManager.filterByAcl(studyRepository.findAll(sort)).toList();
        List<Study> partitionedStudies = Iterables.get(Iterables.partition(studies, size), pageStart);
        int totalElements = partitionedStudies.size();
        if (totalElements > 0) {
            page = new StudyPage(partitionedStudies, pageRequest,
                    totalElements);
        } else {
            page = new StudyPage(partitionedStudies, pageRequest, 0);
        }
        return page;
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
}
