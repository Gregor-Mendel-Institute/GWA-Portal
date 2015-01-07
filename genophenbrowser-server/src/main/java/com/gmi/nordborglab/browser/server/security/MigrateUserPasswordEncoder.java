package com.gmi.nordborglab.browser.server.security;


import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by uemit.seren on 1/7/15.
 */
//Required to implment old PasswordEncoder because legacy MD5 ecnoder needs the salt
// see here: http://stackoverflow.com/questions/17240744/spring-security-3-1-4-and-shapasswordencoder-deprecation/17348888#17348888
@Component
@Transactional(readOnly = true)
public class MigrateUserPasswordEncoder implements PasswordEncoder {


    @Resource
    protected Md5PasswordEncoder legacyEncoder;

    @Resource
    protected UserRepository userRepository;

    protected BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();


    @Override
    public String encodePassword(String rawPass, Object salt) {
        return bcryptEncoder.encode(rawPass);
    }

    @Override
    @Transactional(readOnly = false)
    public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
        if (legacyEncoder.isPasswordValid(encPass, rawPass, salt)) {
            AppUser user = userRepository.findByPassword(encPass);
            user.setPassword(bcryptEncoder.encode(rawPass));
            userRepository.save(user);
            return true;
        }
        return bcryptEncoder.matches(rawPass, encPass);
    }
}
