package com.gmi.nordborglab.browser.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.gmi.nordborglab.browser.server.domain.BaseEntity;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class SecurityUtil {
	
	
	private static AppUserFactory appUserFactory = AutoBeanFactorySource.create(AppUserFactory.class);
	
	public static final String DEFAULT_AUTHORITY = "ROLE_USER";
	
	public static ImmutableSet<String> ALLOWED_AUTHORITIES = ImmutableSet.of("ROLE_ADMIN", "ROLE_USER","ROLE_ANONYMOUS");

	public static Collection<? extends GrantedAuthority> getGrantedAuthorities(List<Authority> authorities) {
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		for (Authority authority : authorities) {
			grantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
		}
		return grantedAuthorities;
	}

	public static CustomUser getUserFromAppUser(AppUser appUser) {
		return new CustomUser(appUser.getFirstname(),appUser.getLastname(),appUser.getEmail(),		
				appUser.getUsername(), 
				appUser.getPassword().toLowerCase(),
				appUser.isEnabled(),
				appUser.isAccountNonExpired(),
				appUser.isCredentialsNonExpired(),
				appUser.isAccountNonLocked(),
				getGrantedAuthorities(appUser.getAuthorities()));
	}
	
	public static String serializeUserToJson(CustomUser user) {
		String json;
		
		List<AuthorityProxy> authorities = new ArrayList<AuthorityProxy>();
		for (GrantedAuthority authority:user.getAuthorities()) {
			AuthorityProxy authorityProxy = appUserFactory.authority().as();
			authorityProxy.setAuthority(authority.getAuthority());
		    authorities.add(authorityProxy);
		}
		AppUserProxy appUserProxy = appUserFactory.appuser().as();
		appUserProxy.setAuthorities(authorities);
		appUserProxy.setFirstname(user.getFirstname());
		appUserProxy.setLastname(user.getLastname());
		appUserProxy.setEmail(user.getEmail());
		AutoBean<AppUserProxy> bean = appUserFactory.appuser(appUserProxy);
		json = AutoBeanCodex.encode(bean).getPayload();
		return json;
	}
	
	public static String getUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            return auth.getPrincipal().toString();
        }
	}
	
	public static Authentication getAuthentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth;
	}
	
	public static List<String> getAuthorities(RoleHierarchy roleHierarchy) {
		List<String> authorities = new ArrayList<String>();
		Authentication user = getAuthentication();
		PrincipalSid sid = new PrincipalSid(user);
		authorities.add(sid.getPrincipal());
		Collection<? extends GrantedAuthority> grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(user.getAuthorities());
		for (GrantedAuthority auth: grantedAuthorities) {
			authorities.add(auth.toString());
		}
		return authorities;
	}
	
	public static <T extends BaseEntity> ImmutableBiMap<ObjectIdentity,T> retrieveObjectIdentites(List<T> entities) {
		Map<ObjectIdentity,T> identities = new HashMap<ObjectIdentity,T>();
		for (T entity: entities) {
			ObjectIdentity oid = new ObjectIdentityImpl(entity.getClass(),entity.getId());
			identities.put(oid,entity);
		}
		return ImmutableBiMap.copyOf(identities);
	}

	public static List<Sid> getSids(RoleHierarchy roleHierarchy) {
		List<Sid> sids = new ArrayList<Sid>();
		Authentication user = getAuthentication();
		sids.add(new PrincipalSid(user));
		Collection<? extends GrantedAuthority> grantedAuthorities = user.getAuthorities();
		if (roleHierarchy != null)
			grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(grantedAuthorities); 
		for (GrantedAuthority auth: grantedAuthorities) {
			sids.add(new GrantedAuthoritySid(auth));
		}
		return sids;
	}

}
