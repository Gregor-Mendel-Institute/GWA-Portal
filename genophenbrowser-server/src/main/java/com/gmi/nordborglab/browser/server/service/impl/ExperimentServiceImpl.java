package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.gmi.nordborglab.browser.server.domain.pages.PublicationPage;
import com.gmi.nordborglab.browser.server.domain.util.Publication;
import com.gmi.nordborglab.browser.server.repository.PublicationRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.google.common.collect.Sets;
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
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import sun.nio.cs.Surrogate;

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
    private AclManager aclManager;

    @Resource
    private RoleHierarchy roleHierarchy;

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
        experiment = aclManager.setPermissionAndOwner(experiment);
        return experiment;
    }


    @Override
    public ExperimentPage findByAcl(int start, int size) {
        List<String> authorities = SecurityUtil.getAuthorities(roleHierarchy);
        PageRequest pageRequest = new PageRequest(start / size, size);
        Page<Experiment> page = experimentRepository.findByAcl(authorities,
                CustomPermission.READ.getMask(), pageRequest);
        return new ExperimentPage(page.getContent(), pageRequest,
                page.getTotalElements());
    }

    @Override
    public List<Experiment> findAllByAcl(Integer permission) {
        List<String> authorities = SecurityUtil.getAuthorities(roleHierarchy);
        List<Experiment> experiments = experimentRepository.findAllByAcl(authorities,
                permission);
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
    public PublicationPage getPublications(int start, int size) {
        PageRequest pageRequest = new PageRequest(start / size, size);
        Page<Publication> page = publicationRepository.findAll(pageRequest);
        return new PublicationPage(page.getContent(), pageRequest,
                page.getTotalElements());
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
}
