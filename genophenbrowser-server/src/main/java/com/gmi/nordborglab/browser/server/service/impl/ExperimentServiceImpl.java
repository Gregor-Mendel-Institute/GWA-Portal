package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.gmi.nordborglab.browser.server.security.CustomPermission;
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
	private TraitUomService traitUomService;

	@Resource
	private MutableAclService aclService;

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
			addPermission(experiment, new PrincipalSid(SecurityUtil.getUsername()),
				permission);
            addPermission(experiment,new GrantedAuthoritySid("ROLE_ADMIN"),permission);
		}
		experiment = setPermissionAndOwner(experiment);
		return experiment;
	}

	public void addPermission(Experiment experiment, Sid recipient,
			Permission permission) {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class,
				experiment.getId());

		try {
			acl = (MutableAcl) aclService.readAclById(oid);
		} catch (NotFoundException nfe) {
			acl = aclService.createAcl(oid);
		}

		acl.insertAce(acl.getEntries().size(), permission, recipient, true);
		aclService.updateAcl(acl);
		logger.debug("Added permission " + permission + " for Sid " + recipient
				+ " Experiment " + experiment);
	}

	@Override
	public ExperimentPage findByAcl(int start, int size) {
		List<String> authorities = SecurityUtil.getAuthorities(roleHierarchy);
		PageRequest pageRequest = new PageRequest(start/size, size);
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
        return experiments;
    }

    @Override
	public Experiment findExperiment(Long id) {
		Experiment experiment = experimentRepository.findOne(id);
		experiment = setPermissionAndOwner(experiment);
		return experiment;
	}
	
	private Experiment setPermissionAndOwner(Experiment experiment) {
		List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class,
				experiment.getId());
		Acl acl = aclService.readAclById(oid, authorities);
		boolean isOwner = false;
		for (Sid sid : authorities) {
			if (sid.equals(acl.getOwner())) {
				isOwner = true;
				break;
			}
		}
		for (AccessControlEntry ace: acl.getEntries()) {
			if (authorities.contains(ace.getSid())) {
				experiment.setUserPermission(new CustomAccessControlEntry((Long)ace.getId(),ace.getPermission().getMask(),ace.isGranting()));
				break;
			}
		}
		
		experiment.setIsOwner(isOwner);
		experiment.setNumberOfPhenotypes(traitUomService.countPhenotypeByExperimentCount(experiment.getId()));
		return experiment;
	}
}
