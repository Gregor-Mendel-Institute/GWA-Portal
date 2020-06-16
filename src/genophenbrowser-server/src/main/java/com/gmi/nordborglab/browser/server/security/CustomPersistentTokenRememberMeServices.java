package com.gmi.nordborglab.browser.server.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by uemit.seren on 12/22/14.
 */
public class CustomPersistentTokenRememberMeServices extends PersistentTokenBasedRememberMeServices {


    public CustomPersistentTokenRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }


    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        String isRegularLogin = request.getParameter("isRegularLogin");
        // username password form based login
        if (isRegularLogin != null && "true".equals(isRegularLogin)) {
            return super.rememberMeRequested(request, parameter);
        } else { // social logjn
            // return always 'true' for social login
            return true;
        }
    }
}
