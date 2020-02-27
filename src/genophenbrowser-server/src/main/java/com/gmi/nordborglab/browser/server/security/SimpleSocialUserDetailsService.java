package com.gmi.nordborglab.browser.server.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * Created by uemit.seren on 12/21/14.
 */


@Service
public class SimpleSocialUserDetailsService implements SocialUserDetailsService {

    @Qualifier("JPAuserDetailsService")
    @Resource
    protected UserDetailsService userDetailService;

    @Override
    public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException, DataAccessException {
        return (SocialUserDetails) userDetailService.loadUserByUsername(userId);
    }
}
