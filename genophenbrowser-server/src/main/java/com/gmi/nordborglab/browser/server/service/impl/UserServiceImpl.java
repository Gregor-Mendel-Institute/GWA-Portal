package com.gmi.nordborglab.browser.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.repository.AclSidRepository;
import com.gmi.nordborglab.browser.server.repository.UserNotificationRepository;
import com.google.common.collect.Lists;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.form.Registration;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.DuplicateRegistrationException;
import com.gmi.nordborglab.browser.server.service.UserService;

@Service("userService")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {


    @Resource
    private UserRepository userRepository;

    @Resource
    private UserNotificationRepository userNotificationRepository;

    @Resource
    private AclSidRepository aclSidRepository;


    @Override
    @Transactional(readOnly = false)
    public void registerUserIfValid(Registration registration,
                                    boolean userIsValid) throws DuplicateRegistrationException {
        if (userRepository.findByEmail(registration.getEmail()) != null) {
            throw new DuplicateRegistrationException();
        }
        if (userIsValid) {
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();

            AppUser appUser = new AppUser(registration.getEmail());
            appUser.setEmail(registration.getEmail());
            appUser.setFirstname(registration.getFirstname());
            appUser.setLastname(registration.getLastname());
            appUser.setOpenidUser(false);
            List<Authority> authorities = new ArrayList<Authority>();
            Authority authority = new Authority();
            authority.setAuthority(SecurityUtil.DEFAULT_AUTHORITY);
            authorities.add(authority);
            appUser.setAuthorities(authorities);
            appUser.setPassword("TEMPORARY");
            userRepository.save(appUser);
            appUser.setPassword(encoder.encodePassword(registration.getPassword(), appUser.getId().toString()));
            userRepository.save(appUser);
            //FIXME workaround because exception is thrown when AclSid doesnt exist and first time permission is added
            AclSid aclSid = new AclSid(true, appUser.getId().toString());
            aclSidRepository.save(aclSid);
        }
    }


}
