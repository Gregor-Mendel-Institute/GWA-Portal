package com.gmi.nordborglab.browser.server.service;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.security.CustomAcl;

public interface PermissionService {

	@PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#experiment,'ADMINISTRATION'))")
	CustomAcl getPermissions(Experiment experiment);
	
	@PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#traitUom,'ADMINISTRATION'))")
	CustomAcl getPermissions(TraitUom traitUom);
	
	@PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#experiment,'ADMINISTRATION'))")
	CustomAcl updatePermissions(Experiment experiment,CustomAcl acl);
	
	@PreAuthorize("hasRole('ROLE_USER')")
	SearchPermissionUserRole searchUserAndRoles(String query);
}
