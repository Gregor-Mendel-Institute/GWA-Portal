package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.security.CustomAcl;

import java.util.List;

public interface PermissionService {

	@PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#object,'ADMINISTRATION'))")
	CustomAcl getPermissions(SecureEntity object);
	

	@PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#experiment,'ADMINISTRATION'))")
	CustomAcl updatePermissions(SecureEntity object,CustomAcl acl);
	
	@PreAuthorize("hasRole('ROLE_USER')")
	SearchPermissionUserRole searchUserAndRoles(String query);

    List<AppUser> findAllUsers();
}
