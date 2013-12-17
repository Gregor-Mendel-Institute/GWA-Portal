package com.gmi.nordborglab.browser.server.service.impl;

import java.io.IOException;
import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import com.gmi.nordborglab.browser.server.domain.SecureEntity;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.errai.ClientComService;
import com.gmi.nordborglab.browser.server.repository.*;
import com.gmi.nordborglab.browser.server.security.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.math3.analysis.function.Exp;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.PermissionPrincipal;
import com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole;
import com.gmi.nordborglab.browser.server.service.PermissionService;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    public static final String GRAVATAR_URL = "http://www.gravatar.com/avatar/";

    @Resource
    protected MutableAclService aclService;

    @Resource
    protected UserRepository userRepository;

    @Resource
    protected UserNotificationRepository userNotificationRepository;

    @Resource
    protected Client client;

    @Resource
    protected TraitUomRepository traitUomRepository;

    @Resource
    protected StudyRepository studyRepository;

    @Resource
    protected AclManager aclManager;

    @Resource
    protected EsAclManager esAclManager;


    @Resource
    private RoleHierarchy roleHierarchy;

    protected CustomAcl getGenericPermissions(Object entity) {
        ObjectIdentity oid = new ObjectIdentityImpl(entity);
        Acl acl = aclService.readAclById(oid);
        List<AccessControlEntry> entries = acl.getEntries();
        List<CustomAccessControlEntry> customEntries = new ArrayList<CustomAccessControlEntry>();
        Sid owner = acl.getOwner();
        for (AccessControlEntry entry : entries) {
            PermissionPrincipal principal = null;
            AppUser user = null;
            Sid sid = entry.getSid();
            if (sid instanceof GrantedAuthoritySid && ((GrantedAuthoritySid) sid).getGrantedAuthority().equalsIgnoreCase("ROLE_ANONYMOUS")) {
                GrantedAuthoritySid authSid = (GrantedAuthoritySid) sid;
                String name = "";
                if (entry.getPermission().getMask() == 0) {
                    name = "Private - Only the people listed below can access";
                } else {
                    name = "Public - Anyone can access";
                }
                principal = new PermissionPrincipal(authSid.getGrantedAuthority(), name, false, false);
            } else if (sid instanceof PrincipalSid) {
                user = userRepository.findOne(Long.parseLong(((PrincipalSid) sid).getPrincipal()));
                principal = new PermissionPrincipal(user.getUsername(), user.getFirstname() + " " + user.getLastname() + " (" + user.getEmail() + ")", true, sid.equals(owner), user.getAvatarHash());
            }
            if (principal != null) {
                customEntries.add(new CustomAccessControlEntry((Long) entry.getId(), entry.getPermission().getMask(), entry.isGranting(), principal));
            }
        }
        Collections.sort(customEntries, new Comparator<CustomAccessControlEntry>() {
            @Override
            public int compare(CustomAccessControlEntry o1, CustomAccessControlEntry o2) {
                if (o1.getPrincipal().getIsUser() == o2.getPrincipal().getIsUser())
                    return 0;
                if (o1.getPrincipal().getIsUser())
                    return 1;
                return -1;
            }
        });

        CustomAcl customAcl = new CustomAcl(customEntries, acl.isEntriesInheriting());
        return customAcl;
    }

    @Override
    public CustomAcl getPermissions(SecureEntity entity) {
        return getGenericPermissions(entity);
    }


    @Override
    @Transactional(readOnly = false)
    public CustomAcl updatePermissions(SecureEntity entity, CustomAcl acl) {
        List<UserNotification> notifications = updateGenericPermissions(entity, acl);
        for (UserNotification notification : notifications) {
            userNotificationRepository.save(notification);
            ClientComService.pushUserNotification(notification.getAppUser().getId().toString(), notification.getAppUser().getEmail(), "permission", 0L);
        }
        indexPermissions(entity);
        return getGenericPermissions(entity);
    }


    private void indexPermissions(SecureEntity entity) {
        indexEntityPermissions(entity);
        ImmutableSet<SecureEntity> childEntities = getChildEntities(entity);
        if (childEntities == null || childEntities.size() == 0)
            return;
        FluentIterable<SecureEntity> filteredEntities = aclManager.filterByAcl(childEntities, Lists.newArrayList(CustomPermission.ADMINISTRATION));
        Map<ObjectIdentity, Acl> acls = aclManager.getAcls(filteredEntities);
        for (SecureEntity child : filteredEntities) {
            ObjectIdentity oid = new ObjectIdentityImpl(child.getClass(), child.getId());
            Acl acl = acls.get(oid);
            if (acl != null && acl.isEntriesInheriting()) {
                indexPermissions(child);
            }
        }
    }

    private ImmutableSet<SecureEntity> getChildEntities(SecureEntity entity) {
        if (entity instanceof Experiment) {
            return ImmutableSet.copyOf(Lists.transform(traitUomRepository.findByExperimentId(entity.getId()), new Function<TraitUom, SecureEntity>() {
                @Nullable
                @Override
                public SecureEntity apply(@Nullable TraitUom input) {
                    return (SecureEntity) input;
                }
            }));
        } else if (entity instanceof TraitUom) {
            return ImmutableSet.copyOf(Lists.transform(studyRepository.findByPhenotypeId(entity.getId()), new Function<Study, SecureEntity>() {
                @Nullable
                @Override
                public SecureEntity apply(@Nullable Study input) {
                    return (SecureEntity) input;
                }
            }));
        }
        return null;
    }

    private void indexEntityPermissions(SecureEntity entity) {
        if (entity.getIndexType() == null)
            return;
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();

            builder.startObject();
            esAclManager.addACLAndOwnerContent(builder, aclManager.getAcl(entity));
            builder.endObject();
            UpdateRequestBuilder request = client.prepareUpdate(esAclManager.getIndex(), entity.getIndexType(), entity.getId().toString())
                    .setDoc(builder);
            if (entity.getRouting() != null) {
                request.setRouting(entity.getRouting());
            }
            request.execute();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    protected List<UserNotification> updateGenericPermissions(SecureEntity entity, CustomAcl acl) {
        List<UserNotification> notifications = Lists.newArrayList();
        AppUser owner = userRepository.findOne(Long.parseLong(SecurityUtil.getUsername()));
        ObjectIdentity oid = new ObjectIdentityImpl(entity);
        AclImpl currentAcl = (AclImpl) aclService.readAclById(oid);
        currentAcl.setEntriesInheriting(acl.getIsEntriesInheriting());
        // update existing or delete them
        for (int i = 0; i < currentAcl.getEntries().size(); i++) {
            final AccessControlEntryImpl ace = (AccessControlEntryImpl) currentAcl.getEntries().get(i);
            if (ace.getSid() instanceof GrantedAuthoritySid && ((GrantedAuthoritySid) ace.getSid()).getGrantedAuthority().equals("ROLE_ADMIN"))
                continue;
            CustomAccessControlEntry customAce = Iterables.find(acl.getEntries(), new Predicate<CustomAccessControlEntry>() {
                @Override
                public boolean apply(@Nullable CustomAccessControlEntry customAccessControlEntry) {
                    if (customAccessControlEntry == null)
                        return false;
                    return customAccessControlEntry.getId().equals(ace.getId());
                }
            }, null);

            if (customAce == null) {
                currentAcl.deleteAce(i);
            } else {
                if (customAce.getMask() != ace.getPermission().getMask())
                    currentAcl.updateAce(i, new CustomPermission(customAce.getMask()));
            }
            acl.getEntries().remove(customAce);
        }


        for (CustomAccessControlEntry newAce : acl.getEntries()) {
            boolean isDuplicate = false;
            Sid sid = null;
            if (newAce.getPrincipal().getIsUser()) {
                if (userRepository.findOne(Long.parseLong(newAce.getPrincipal().getId())) == null)
                    break;
                for (AccessControlEntry ace : currentAcl.getEntries()) {
                    if (ace.getSid() instanceof PrincipalSid) {
                        PrincipalSid checkSid = (PrincipalSid) ace.getSid();
                        if (checkSid.getPrincipal().equals(newAce.getPrincipal().getId())) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
                sid = new PrincipalSid(newAce.getPrincipal().getId());
            } else {
                if (!SecurityUtil.ALLOWED_AUTHORITIES.contains(newAce.getPrincipal().getId()))
                    break;
                for (AccessControlEntry ace : currentAcl.getEntries()) {
                    if (ace.getSid() instanceof GrantedAuthoritySid) {
                        GrantedAuthoritySid checkSid = (GrantedAuthoritySid) ace.getSid();
                        if (checkSid.getGrantedAuthority().equals(newAce.getPrincipal().getId())) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }
                sid = new GrantedAuthoritySid(newAce.getPrincipal().getId());
            }
            if (!isDuplicate) {
                //TODO add user notification
                currentAcl.insertAce(currentAcl.getEntries().size(), new CustomPermission(newAce.getMask()), sid, true);
                if (sid instanceof PrincipalSid) {
                    AppUser user = userRepository.findOne(Long.parseLong(((PrincipalSid) sid).getPrincipal()));
                    UserNotification notification = new UserNotification();
                    notification.setAppUser(user);
                    notification.setType("permission");
                    String link = "";
                    String userIcon = "<img class=\"img-circle\" src=\"" + GRAVATAR_URL + owner.getAvatarHash() + "&s=29\" />";
                    String objType = "";
                    String objName = "";

                    if (entity instanceof Experiment) {
                        link = "#!study/" + entity.getId() + "/overview";
                        objType = "study";
                        objName = ((Experiment) entity).getName();
                    } else if (entity instanceof GWASResult) {
                        link = "#!gwasViewer;id=" + entity.getId();
                        objType = "GWAS-result";
                        objName = ((GWASResult) entity).getName();
                    } else if (entity instanceof CandidateGeneList) {
                        link = "#!meta/candidategenelist/" + entity.getId();
                        objType = "candidategenelist";
                        objName = ((CandidateGeneList) entity).getName();
                    }
                    String notificationText = userIcon + " <span><a href=\"#!profile/" + owner.getId().toString() + "\">%s</a> shared a <a href=\"%s\">%s (%s)</a> with you</span>";
                    notification.setText(String.format(notificationText, owner.getFirstname() + " " + owner.getLastname(), link, objType, objName));
                    notifications.add(notification);
                }
            }
        }
        aclService.updateAcl(currentAcl);
        return notifications;
    }

    @Override
    public List<AppUser> findAllUsers() {
        //TODO think about security
        List<AppUser> users = (List<AppUser>) userRepository.findAll();
        return users;
    }


    @Override
    public SearchPermissionUserRole searchUserAndRoles(String query) {
        /*PermissionPrincipal principal = null;
        SearchPermissionUserRole result = new SearchPermissionUserRole();
		List<PermissionPrincipal> principals = new ArrayList<PermissionPrincipal>();
		if ("ROLE_ADMIN".toLowerCase().contains(query.toLowerCase()))
			principals.add(new PermissionPrincipal("ROLE_ADMIN", "ROLE ADMIN", false));
		if ("ROLE_ANONYMOUS".toLowerCase().contains(query.toLowerCase()))
			principals.add(new PermissionPrincipal("ROLE_ANONYMOUS", "ROLE ANONYMOUS", false));
		if ("ROLE_USER".toLowerCase().contains(query.toLowerCase()))
			principals.add(new PermissionPrincipal("ROLE_USER", "ROLE USER", false));
		List<AppUser> users = userRepository.findAll(where(firstNameIsLike(query)).or(lastNameIsLike(query))); 
		for (AppUser user : users) {
			principals.add(new PermissionPrincipal(user.getUsername(), user.getFirstname()+" "+ user.getLastname(), true));
		}
		result.setPrincipals(principals);
		return result; */
        return null;
    }

}
