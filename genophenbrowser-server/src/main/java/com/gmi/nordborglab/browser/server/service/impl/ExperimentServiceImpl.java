package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
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
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;

@Service
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
	public Experiment save(Experiment experiment) {
		experiment = experimentRepository.save(experiment);
		if (experiment.getId() == null) {
			CumulativePermission permission = new CumulativePermission();
			permission.set(BasePermission.ADMINISTRATION);
			addPermission(experiment, new PrincipalSid(SecurityUtil.getUsername()),
				permission);
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
		PageRequest pageRequest = new PageRequest(start, size);
		Page<Experiment> page = experimentRepository.findByAcl(authorities,
				BasePermission.READ.getMask(), pageRequest);
		return new ExperimentPage(page.getContent(), pageRequest,
				page.getTotalElements());
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
