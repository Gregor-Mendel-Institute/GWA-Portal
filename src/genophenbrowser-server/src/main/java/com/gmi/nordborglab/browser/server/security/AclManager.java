package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.google.common.collect.FluentIterable;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

/**
 * Created by uemit.seren on 7/8/15.
 */
public interface AclManager {
    <T extends SecureEntity> boolean hasPermission(Authentication user, T entity, String permission);

    <T extends SecureEntity> boolean hasPermission(Authentication user, T entity, Permission permission);

    <T extends SecureEntity> Map<ObjectIdentity, Acl> getAcls(Iterable<T> entities);

    <T extends SecureEntity> Acl getAcl(T entity);

    <T extends SecureEntity> AppUser getAppUserFromEntity(T entity);

    <T extends SecureEntity> List<T> setPermissionAndOwners(List<T> entities);

    <T extends SecureEntity> T setPermissionAndOwner(T entity);

    <T extends SecureEntity, S extends SecureEntity> void addPermission(T entity, Sid recipient,
                                                                        Permission permission, Class<S> parentClass, Long parentId);

    <T extends SecureEntity, S extends SecureEntity> void addPermission(T entity, Sid recipient,
                                                                        Permission permission, S parentEntity);

    <T extends SecureEntity> FluentIterable<T> filterByAcl(Iterable<T> entities);

    <T extends SecureEntity> FluentIterable<T> filterByAcl(Iterable<T> entities, List<Permission> permissions);

    <T extends SecureEntity> FluentIterable<T> filterByAcl(Iterable<T> entities, List<Permission> permissions, List<Sid> authorities);

    <T extends SecureEntity> void deletePermissions(T entity, boolean deleteChildren);
}
