package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;

import javax.annotation.Resource;

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

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.TraitRepository;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.TraitService;
import com.google.common.collect.ImmutableList;

@Service
@Transactional(readOnly = true)
public class TraitServiceImpl implements TraitService {

	@Resource
	protected RoleHierarchy roleHierarchy;
	
	@Resource
	protected MutableAclService aclService;
	
	@Resource
	protected TraitRepository traitRepository;
	
	@Override
	public List<Trait> findAllTraitValues(Long phenotypeId, Long alleleAssayId,Long statisticTypeId) {
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
		return traitRepository.findAllTraitValues(phenotypeId, alleleAssayId,statisticTypeId);
	}

	@Override
	public List<Trait> findAllTraitValuesByStatisticType(Long phenotypeId,
			Long statiticTypeId) {
		return traitRepository.findByTraitUomIdAndStatisticTypeId(phenotypeId,statiticTypeId);
	}

}
