package com.gmi.nordborglab.browser.server.service.impl;

import static com.gmi.nordborglab.browser.server.domain.specifications.AppUserSpecifications.firstNameIsLike;
import static com.gmi.nordborglab.browser.server.domain.specifications.AppUserSpecifications.lastNameIsLike;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.PermissionPrincipal;
import com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.CustomAcl;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.PermissionService;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
	
	@Resource
	protected MutableAclService aclService;
	
	@Resource 
	protected UserRepository userRepository;
	
	
	@Resource
	private RoleHierarchy roleHierarchy;

	protected CustomAcl getGenericPermissions(Object entity) {
		ObjectIdentity oid = new ObjectIdentityImpl(entity);
		Acl acl = aclService.readAclById(oid);
		List<AccessControlEntry> entries =  acl.getEntries();
		List<CustomAccessControlEntry> customEntries = new ArrayList<CustomAccessControlEntry>();
		for (AccessControlEntry entry:entries) {
			PermissionPrincipal principal = null;
			AppUser user = null;
			Sid sid = entry.getSid();
			if (sid instanceof GrantedAuthoritySid) {
				GrantedAuthoritySid authSid = (GrantedAuthoritySid)sid;
				principal = new PermissionPrincipal(authSid.getGrantedAuthority(),authSid.getGrantedAuthority(),false);
			}
			else if (sid instanceof PrincipalSid) {
				user = userRepository.findOne(((PrincipalSid)sid).getPrincipal());
				principal = new PermissionPrincipal(user.getUsername(),user.getFirstname() +" "+user.getLastname(),false);
			}
			customEntries.add(new CustomAccessControlEntry((Long)entry.getId(),entry.getPermission().getMask(), entry.isGranting(), principal));
		}
		CustomAcl customAcl = new CustomAcl(customEntries,acl.isEntriesInheriting());
		return customAcl;
	}

	@Override
	public CustomAcl getPermissions(Experiment experiment) {
		return getGenericPermissions(experiment);
	}

	@Override
	public CustomAcl getPermissions(TraitUom traitUom) {
		return getGenericPermissions(traitUom);
	}

	@Override
	public CustomAcl updatePermissions(Experiment experiment, CustomAcl acl) {
		updateGenericPermissions(experiment,acl);
		return getGenericPermissions(experiment);
	}
	
	@Transactional(readOnly=false)
	protected void updateGenericPermissions(Object entity,CustomAcl acl) {
		ObjectIdentity oid = new ObjectIdentityImpl(entity);
		AclImpl currentAcl = (AclImpl)aclService.readAclById(oid);
		currentAcl.setEntriesInheriting(acl.getIsEntriesInheriting());
		for (int i =0;i<currentAcl.getEntries().size();i++) {
			AccessControlEntryImpl ace  = (AccessControlEntryImpl)currentAcl.getEntries().get(i);
			for (int j = 0;j<acl.getEntries().size();j++) {
				CustomAccessControlEntry customAce = acl.getEntries().get(j); 
				if (ace.getId().equals(customAce.getId())) {
					if (customAce.getMask() == 0) {
						currentAcl.deleteAce(i);
					}
					else if (customAce.getMask() != ace.getPermission().getMask()) {
						currentAcl.updateAce(i, new CustomPermission(customAce.getMask()));
					}
					acl.getEntries().remove(j);
					break;
				}
			}
		}
		for (CustomAccessControlEntry newAce: acl.getEntries()) {
			boolean isDuplicate = false;
			Sid sid = null;
			
			if (newAce.getPrincipal().getIsUser()) {
				if (userRepository.findOne(newAce.getPrincipal().getId()) == null)
					break;
				for (AccessControlEntry ace: currentAcl.getEntries()) {
					if (ace.getSid() instanceof PrincipalSid) {
						PrincipalSid checkSid = (PrincipalSid)ace.getSid();
						if (checkSid.getPrincipal().equals(newAce.getPrincipal().getId())) {
							isDuplicate = true;
							break;
						}
					}
				}
				sid = new PrincipalSid(newAce.getPrincipal().getId());
			}
			else {
				if (!SecurityUtil.ALLOWED_AUTHORITIES.contains(newAce.getPrincipal().getId()))
					break;
				for (AccessControlEntry ace: currentAcl.getEntries()) {
					if (ace.getSid() instanceof GrantedAuthoritySid) {
						GrantedAuthoritySid checkSid = (GrantedAuthoritySid)ace.getSid();
						if (checkSid.getGrantedAuthority().equals(newAce.getPrincipal().getId())) {
							isDuplicate = true;
							break;
						}
					}
				}
				sid = new GrantedAuthoritySid(newAce.getPrincipal().getId());
			}
			if (!isDuplicate)
				currentAcl.insertAce(currentAcl.getEntries().size(), new CustomPermission(newAce.getMask()), sid, newAce.getIsGranting());
		}
		aclService.updateAcl(currentAcl);
	}

	@Override
	public SearchPermissionUserRole searchUserAndRoles(String query) {
		PermissionPrincipal principal = null;
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
		return result;
	}

}
