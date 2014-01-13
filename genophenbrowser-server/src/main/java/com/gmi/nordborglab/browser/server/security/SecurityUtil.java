package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecurityUtil {


    private static AppUserFactory appUserFactory = AutoBeanFactorySource.create(AppUserFactory.class);

    public static final String DEFAULT_AUTHORITY = "ROLE_USER";

    public static ImmutableSet<String> ALLOWED_AUTHORITIES = ImmutableSet.of("ROLE_ADMIN", "ROLE_USER", "ROLE_ANONYMOUS");

    public static Function<Sid, String> sid2String = new Function<Sid, String>() {
        @Nullable
        @Override
        public String apply(@Nullable Sid sid) {
            if (sid instanceof PrincipalSid) {
                return ((PrincipalSid) sid).getPrincipal();
            } else if (sid instanceof GrantedAuthoritySid) {
                return ((GrantedAuthoritySid) sid).getGrantedAuthority();
            }
            return null;
        }
    };

    public static Collection<? extends GrantedAuthority> getGrantedAuthorities(List<Authority> authorities) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        for (Authority authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        return grantedAuthorities;
    }

    public static CustomUser getUserFromContext() {
        Authentication auth = getAuthentication();
        if (auth.getPrincipal() instanceof CustomUser) {
            return (CustomUser) auth.getPrincipal();
        }
        return null;
    }

    public static CustomUser getUserFromAppUser(AppUser appUser) {
        return new CustomUser(appUser,
                getGrantedAuthorities(appUser.getAuthorities()));
    }

    public static String serializeUserToJson(CustomUser user) {
        String json;

        List<AuthorityProxy> authorities = new ArrayList<AuthorityProxy>();
        for (GrantedAuthority authority : user.getAuthorities()) {
            AuthorityProxy authorityProxy = appUserFactory.authority().as();
            authorityProxy.setAuthority(authority.getAuthority());
            authorities.add(authorityProxy);
        }
        AppUserProxy appUserProxy = appUserFactory.appuser().as();
        appUserProxy.setAuthorities(authorities);
        appUserProxy.setFirstname(user.getFirstname());
        appUserProxy.setLastname(user.getLastname());
        appUserProxy.setEmail(user.getEmail());
        appUserProxy.setId(user.getId());
        appUserProxy.setAvatarSource(user.getAppUser().getAvatarSource());
        appUserProxy.setGravatarHash(DigestUtils.md5Hex(user.getEmail().toLowerCase().trim()));
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
        for (GrantedAuthority auth : grantedAuthorities) {
            authorities.add(auth.toString());
        }
        return authorities;
    }

    public static boolean isAdmin(RoleHierarchy roleHierarchy) {
        Collection<? extends GrantedAuthority> grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(getAuthentication().getAuthorities());
        boolean isAdmin = false;
        for (GrantedAuthority auth : grantedAuthorities) {
            if (auth.getAuthority().equals("ROLE_ADMIN")) {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }

    public static List<Sid> getSids(RoleHierarchy roleHierarchy) {
        List<Sid> sids = new ArrayList<Sid>();
        Authentication user = getAuthentication();
        sids.add(new PrincipalSid(user));
        Collection<? extends GrantedAuthority> grantedAuthorities = user.getAuthorities();
        if (roleHierarchy != null)
            grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(grantedAuthorities);
        for (GrantedAuthority auth : grantedAuthorities) {
            sids.add(new GrantedAuthoritySid(auth));
        }
        return sids;
    }

    public static void updateAppUser(AppUser appUser) {
        CustomUser user = getUserFromAppUser(appUser);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }
}
