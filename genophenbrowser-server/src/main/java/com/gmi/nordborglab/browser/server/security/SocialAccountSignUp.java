package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.form.Registration;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.service.DuplicateRegistrationException;
import com.gmi.nordborglab.browser.server.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by uemit.seren on 12/21/14.
 */

@Component
public class SocialAccountSignUp implements ConnectionSignUp {

    @Resource
    protected UserService userService;

    @Resource
    protected UserRepository userRepository;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SocialAccountSignUp.class);


    @Override
    @Transactional(readOnly = false)
    public String execute(Connection<?> connection) {
        UserProfile profile = connection.fetchUserProfile();
        AppUser appUser = userRepository.findByUsername(profile.getEmail());
        if (profile.getEmail() == null || profile.getEmail().isEmpty()) {
            return null;
        }
        if (appUser == null) {
            try {
                Registration registraion = new Registration();
                registraion.setFirstname(profile.getFirstName());
                registraion.setLastname(profile.getLastName());
                registraion.setEmail(profile.getEmail());
                registraion.setSocialAccount(true);
                appUser = userService.registerUserIfValid(registraion, true);
            } catch (DuplicateRegistrationException e) {
                logger.error("User already existis", e);
                return null;
            }
        }
        return appUser.getUsername();
    }
}
