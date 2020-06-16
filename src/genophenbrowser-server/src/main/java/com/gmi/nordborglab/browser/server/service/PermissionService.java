package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole;
import com.gmi.nordborglab.browser.server.security.CustomAcl;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface PermissionService {

    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#entity,'ADMINISTRATION'))")
    CustomAcl getPermissions(SecureEntity entity);


    @PreAuthorize("hasRole('ROLE_USER') and (hasPermission(#entity,'ADMINISTRATION'))")
    CustomAcl updatePermissions(SecureEntity entity, CustomAcl acl);

    @PreAuthorize("hasRole('ROLE_USER')")
    SearchPermissionUserRole searchUserAndRoles(String query);

    List<AppUser> findAllUsers();
}
