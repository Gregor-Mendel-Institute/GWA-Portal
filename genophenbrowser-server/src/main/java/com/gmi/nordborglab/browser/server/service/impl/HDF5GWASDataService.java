package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Component;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.data.hdf5.HDF5GWASReader;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Component
public class HDF5GWASDataService implements GWASDataService {

	@Resource
	protected TraitUomRepository traitUomRepository;
	
	@Resource
	protected MutableAclService aclService;
	
	@Resource
	protected RoleHierarchy roleHierarchy;
	
	protected GWASReader gwasReader;
	
	@Override
	public ImmutableMap<String,GWASData> getGWASData(Long studyId) {
		TraitUom trait = traitUomRepository.findByStudyId(studyId);
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(BasePermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,trait.getId());
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		if (gwasReader == null) {
			gwasReader = new HDF5GWASReader("/net/gmi.oeaw.ac.at/gwasapp");
		}
		Map<String,GWASData> map = gwasReader.readAll(studyId+".hdf5", 0.05);
		return ImmutableMap.copyOf(map);
	}

}
