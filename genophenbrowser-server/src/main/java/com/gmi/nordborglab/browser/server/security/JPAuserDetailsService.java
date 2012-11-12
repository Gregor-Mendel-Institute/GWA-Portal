package com.gmi.nordborglab.browser.server.security;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class JPAuserDetailsService implements UserDetailsService {

	@Resource
	private UserRepository userRepository;
	

	
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			AppUser appUser = userRepository.findOne(username);
			if (appUser == null) {
				throw new UsernameNotFoundException(username+ " not found");
			}

			return SecurityUtil.getUserFromAppUser(appUser);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}