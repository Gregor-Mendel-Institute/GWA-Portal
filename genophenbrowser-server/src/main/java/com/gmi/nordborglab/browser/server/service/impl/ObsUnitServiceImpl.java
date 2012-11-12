package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.ObsUnitPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.ObsUnitRepository;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.ObsUnitService;
import com.google.common.collect.ImmutableList;

@Service
@Transactional(readOnly = true)
public class ObsUnitServiceImpl implements ObsUnitService {

	@Resource
	protected ObsUnitRepository obsUnitRepository;

	@Resource
	protected RoleHierarchy roleHierarchy;
	
	@Resource
	protected MutableAclService aclService;

	@Override
	public ObsUnitPage findObsUnits(Long id, int start, int size) {
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(BasePermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,id);
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		ObsUnitPage page = null;
		if (start > 0)
			start = start/size;
		PageRequest pageRequest = new PageRequest(start, size);
		Page<ObsUnit> obsUnitPage = obsUnitRepository.findByPhenotypeId(id,
				pageRequest);
		page = new ObsUnitPage(obsUnitPage.getContent(), pageRequest,
				obsUnitPage.getTotalElements());
		return page;
	}

	@Override
	public List<ObsUnit> findObsUnitWithNoGenotype(Long phenotypeId,
			Long alleleAssayId) {
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(BasePermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,phenotypeId);
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		return obsUnitRepository.findAllWithNoGenotype(phenotypeId, alleleAssayId);
	}

}
