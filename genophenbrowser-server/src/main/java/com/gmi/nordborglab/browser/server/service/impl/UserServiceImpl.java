package com.gmi.nordborglab.browser.server.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

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
@Transactional(readOnly=true)
public class UserServiceImpl implements UserService {
	
	@Resource
	UserRepository userRepository;

	
	@Override
	@Transactional(readOnly=false)
	public void registerUserIfValid(Registration registration,
			boolean userIsValid) throws DuplicateRegistrationException {
		if (userRepository.findOne(registration.getEmail())!= null || userRepository.findByEmail(registration.getEmail())!=null) {
			throw new DuplicateRegistrationException();
		}
		if (userIsValid) {
			Md5PasswordEncoder encoder = new Md5PasswordEncoder();
			
			AppUser appUser = new AppUser(registration.getEmail());
			appUser.setEmail(registration.getEmail());
			appUser.setFirstname(registration.getFirstname());
			appUser.setLastname(registration.getLastname());
			appUser.setOpenidUser(false);
			appUser.setPassword(encoder.encodePassword(registration.getPassword(),null));
			List<Authority> authorities = new ArrayList<Authority>();
			Authority authority = new Authority();
			authority.setAuthority(SecurityUtil.DEFAULT_AUTHORITY);
			authorities.add(authority);
			appUser.setAuthorities(authorities);
			userRepository.save(appUser);
		}
	}

}
