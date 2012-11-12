package com.gmi.nordborglab.browser.server.testutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtils {

	public static String TEST_USERNAME = "test@test.at";
	public static String TEST_PASSWORD = "test";
	
	public static void makeActiveUser(String username,String password,Collection<? extends GrantedAuthority> authorities) {
		Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password,authorities);
    	SecurityContextHolder.getContext().setAuthentication(authRequest);
	}
	
	public static void  setAnonymousUser() {
		List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
		Authentication authRequest = new AnonymousAuthenticationToken("TEST", "anonymousUser", authorities);
    	SecurityContextHolder.getContext().setAuthentication(authRequest);
	}
	
	public static void clearContext() {
		SecurityContextHolder.clearContext();
	}
}
