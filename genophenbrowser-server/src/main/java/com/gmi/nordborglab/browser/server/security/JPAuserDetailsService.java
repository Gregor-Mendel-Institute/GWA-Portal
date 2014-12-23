package com.gmi.nordborglab.browser.server.security;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(readOnly = true)
public class JPAuserDetailsService implements UserDetailsService {

    @Resource
    private UserRepository userRepository;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username);
        try {
            // FIXME Because we currently use the ID as username and a PersistentTokenRememberMeService uses the id instead of username
            if (appUser == null) {
                try {
                    Long id = Long.valueOf(username);
                    appUser = userRepository.findOne(id);
                } catch (NumberFormatException e) {

                }
            }
            if (appUser == null) {
                throw new UsernameNotFoundException(username + " not found");
            }

            return SecurityUtil.getUserFromAppUser(appUser);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}