package com.gmi.nordborglab.browser.server.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.repository.AclSidRepository;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.repository.UserRepository;

public class OpenIdUserDetailsService implements UserDetailsService,
        AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

    @Resource
    protected UserRepository userRepository;

    @Resource
    protected EntityManager entityManager;

    @Resource
    protected AclSidRepository aclSidRepository;


    // private final Map<String,CustomUserDetails> registeredUsers = new
    // HashMap<String, CustomUserDetails>();


    @Override
    public UserDetails loadUserByUsername(String id)
            throws UsernameNotFoundException {

        AppUser appUser = userRepository.findOne(Long.parseLong(id));

        if (appUser == null) {
            throw new UsernameNotFoundException(id);
        }

        return SecurityUtil.getUserFromAppUser(appUser);
    }

    @Override
    @Transactional
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) {
        String id = token.getIdentityUrl();

        AppUser appUser = userRepository.findByUsername(id);
        if (appUser != null) {
            return SecurityUtil.getUserFromAppUser(appUser);
        }

        String email = null;
        String firstName = null;
        String lastName = null;
        String fullName = null;

        List<OpenIDAttribute> attributes = token.getAttributes();

        for (OpenIDAttribute attribute : attributes) {
            if (attribute.getName().equals("email")) {
                email = attribute.getValues().get(0);
            }

            if (attribute.getName().equals("firstname")) {
                firstName = attribute.getValues().get(0);
            }

            if (attribute.getName().equals("lastname")) {
                lastName = attribute.getValues().get(0);
            }

            if (attribute.getName().equals("fullname")) {
                fullName = attribute.getValues().get(0);
            }
        }

        if (fullName == null) {
            StringBuilder fullNameBldr = new StringBuilder();

            if (firstName != null) {
                fullNameBldr.append(firstName);
            }

            if (lastName != null) {
                fullNameBldr.append(" ").append(lastName);
            }
            fullName = fullNameBldr.toString();
        }
        //FIXME check if there is one with the email address because server hoist changed
        if (appUser == null) {
            appUser = userRepository.findByEmail(email);
            if (appUser != null && appUser.openidUser()) {
                appUser.setUsername(id);
                userRepository.save(appUser);
                return SecurityUtil.getUserFromAppUser(appUser);
            }
        }
        appUser = new AppUser(id);
        appUser.setEmail(email);
        appUser.setPassword("unused");
        appUser.setFirstname(firstName);
        appUser.setLastname(lastName);
        appUser.setOpenidUser(true);
        List<Authority> authorities = new ArrayList<Authority>();
        Authority authority = new Authority();
        authority.setAuthority(SecurityUtil.DEFAULT_AUTHORITY);
        authorities.add(authority);
        appUser.setAuthorities(authorities);
        userRepository.save(appUser);

        //FIXME WORKAROUND for  http://forum.springsource.org/showthread.php?55490-ACL-Transaction-must-be-running
        AclSid sid = aclSidRepository.findBySid(appUser.getId().toString());
        if (sid == null) {
            sid = new AclSid(true, appUser.getId().toString());
            aclSidRepository.save(sid);
        }
        return SecurityUtil.getUserFromAppUser(appUser);
    }
}
