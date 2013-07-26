package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 01.07.13
 * Time: 13:20
 * To change this template use File | Settings | File Templates.
 */

@Component
public class AclManager {

    @Resource
    protected RoleHierarchy roleHierarchy;

    @Resource
    protected MutableAclService aclService;

    @Resource
    protected UserRepository userRepository;


    public <T extends SecureEntity> Acl getAcl(T entity) {
        ObjectIdentity oid = new ObjectIdentityImpl(entity.getClass(), entity.getId());
        return aclService.readAclById(oid);
    }


    public <T extends SecureEntity> List<T> setPermissionAndOwners(List<T> entities) {
        List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        if (entities.size() > 0) {
            final ImmutableBiMap<ObjectIdentity, T> identities = retrieveObjectIdentites(entities);
            final ImmutableMap<ObjectIdentity, Acl> acls = ImmutableMap.copyOf(aclService.readAclsById(identities.keySet().asList(), authorities));
            for (Map.Entry<ObjectIdentity, T> entry : identities.entrySet()) {
                Acl acl = acls.get(entry.getKey());
                SecureEntity entity = entry.getValue();
                setPermissionAndOwner(entity, acl, authorities);
            }
        }
        return entities;
    }

    public <T extends SecureEntity> T setPermissionAndOwner(T entity) {
        List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        ObjectIdentity oid = new ObjectIdentityImpl(entity.getClass(), entity.getId());
        Acl acl = aclService.readAclById(oid, authorities);
        entity = setPermissionAndOwner(entity, acl, authorities);
        return entity;
    }

    private <T extends SecureEntity> T setPermissionAndOwner(T entity, Acl acl, List<Sid> authorities) {
        entity.setOwnerUser(getAppUserFromSid(acl.getOwner()));
        for (Sid sid : authorities) {
            if (sid.equals(acl.getOwner())) {
                entity.setIsOwner(true);
                break;
            }
        }
        setPermission(entity, acl, authorities);
        return entity;
    }

    private AppUser getAppUserFromSid(Sid sid) {
        Long sidId = null;
        if (sid instanceof GrantedAuthoritySid) {
            AppUser user = new AppUser();
            user.setFirstname("Admin");
            user.setLastname("");
            return user;
        } else if (sid instanceof PrincipalSid) {
            try {
                sidId = Long.parseLong(((PrincipalSid) sid).getPrincipal());
            } catch (Exception e) {
            }
        }
        if (sidId == null) {
            return null;
        }
        return userRepository.findOne(sidId);
    }

    private <T extends SecureEntity> T setPermission(T entity, Acl acl, List<Sid> authorities) {
        for (AccessControlEntry ace : acl.getEntries()) {
            if ((ace.getSid() instanceof GrantedAuthoritySid && ((GrantedAuthoritySid) ace.getSid()).getGrantedAuthority().equalsIgnoreCase("ROLE_ANONYMOUS"))) {
                entity.setIsPublic(true);
            }
            if (authorities.contains(ace.getSid())) {
                entity.setUserPermission(new CustomAccessControlEntry((Long) ace.getId(), ace.getPermission().getMask(), ace.isGranting()));
            }
        }
        if (acl.isEntriesInheriting() && acl.getParentAcl() != null) {
            entity = setPermission(entity, acl.getParentAcl(), authorities);
        }
        return entity;
    }

    public <T extends SecureEntity, S extends SecureEntity> void addPermission(T entity, Sid recipient,
                                                                               Permission permission, Class<S> parentClass, Long parentId) {
        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(entity.getClass(),
                entity.getId());

        Acl parentAcl = null;
        if (parentClass != null && parentId != null) {
            parentAcl = aclService.readAclById(new ObjectIdentityImpl(parentClass, parentId));
        }

        try {
            acl = (MutableAcl) aclService.readAclById(oid);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oid);
        }
        acl.setParent(parentAcl);
        if (parentAcl != null) {
            acl.setEntriesInheriting(true);
        }
        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        aclService.updateAcl(acl);
    }

    public <T extends SecureEntity, S extends SecureEntity> void addPermission(T entity, Sid recipient,
                                                                               Permission permission, S parentEntity) {
        Class<? extends SecureEntity> parentClass = null;
        Long parentId = null;
        if (parentEntity != null) {
            parentClass = parentEntity.getClass();
            parentId = parentEntity.getId();
        }

        addPermission(entity, recipient, permission, parentClass, parentId);
    }

    public <T extends SecureEntity> FluentIterable<T> filterByAcl(List<T> entities) {
        return filterByAcl(entities, ImmutableList.of(CustomPermission.READ));
    }

    public <T extends SecureEntity> FluentIterable<T> filterByAcl(List<T> entities, final List<Permission> permissions) {
        return filterByAcl(entities, ImmutableList.of(CustomPermission.READ), SecurityUtil.getSids(roleHierarchy));
    }


    public <T extends SecureEntity> FluentIterable<T> filterByAcl(List<T> entities, final List<Permission> permissions, final List<Sid> authorities) {

        FluentIterable<T> filtered = FluentIterable.from(entities);
        if (filtered.size() > 0) {
            final ImmutableBiMap<T, ObjectIdentity> identities = retrieveObjectIdentites(filtered.toList()).inverse();
            final ImmutableMap<ObjectIdentity, Acl> acls = ImmutableMap.copyOf(aclService.readAclsById(identities.values().asList(), authorities));

            Predicate<T> predicate = new Predicate<T>() {

                @Override
                public boolean apply(T entity) {
                    boolean flag = false;
                    ObjectIdentity identity = identities.get(entity);
                    if (acls.containsKey(identity)) {
                        Acl acl = acls.get(identity);
                        try {
                            if (acl.isGranted(permissions, authorities, false))
                                flag = true;
                        } catch (NotFoundException e) {

                        }
                    }
                    return flag;
                }
            };
            filtered = filtered.filter(predicate);
        }
        return filtered;
    }

    public <T extends SecureEntity> void deleteAcl(T entity) {
        ObjectIdentity oid = new ObjectIdentityImpl(entity.getClass(), entity.getId());
        aclService.deleteAcl(oid, true);
    }

    public static <T extends SecureEntity> ImmutableBiMap<ObjectIdentity, T> retrieveObjectIdentites(List<T> entities) {
        Map<ObjectIdentity, T> identities = new HashMap<ObjectIdentity, T>();
        for (T entity : entities) {
            ObjectIdentity oid = new ObjectIdentityImpl(entity.getClass(), entity.getId());
            identities.put(oid, entity);
        }
        return ImmutableBiMap.copyOf(identities);
    }

    public <T extends SecureEntity> void deletePermissions(T entity, boolean deleteChildren) {
        aclService.deleteAcl(new ObjectIdentityImpl(entity.getClass(), entity.getId()), deleteChildren);
    }
}
