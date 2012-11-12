package com.gmi.nordborglab.browser.client.testutils;

import java.util.ArrayList;
import java.util.List;

import com.gmi.nordborglab.browser.client.testutils.proxys.AppUserProxyHelper;
import com.gmi.nordborglab.browser.client.testutils.proxys.AuthorityProxyHelper;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;
import com.google.common.collect.ImmutableList;

public class SecurityUtils {
	

	public static AppUserProxy createAdminUser() {
		AppUserProxy userProxy = AppUserProxyHelper.createProxy();
		List<AuthorityProxy> authorities = new ArrayList<AuthorityProxy>();
		AuthorityProxy authorityProxy = AuthorityProxyHelper.createProxy();
		authorityProxy.setAuthority("ROLE_ADMIN");
		authorities.add(authorityProxy);
		userProxy.setAuthorities(authorities);
		userProxy.setFirstname("Admin");
		userProxy.setLastname("Admin");
		userProxy.setEmail("admin@admin.at");
		return userProxy;
	}
	
	
	public static AppUserProxy createUser() {
		AppUserProxy userProxy = AppUserProxyHelper.createProxy();
		List<AuthorityProxy> authorities = new ArrayList<AuthorityProxy>();
		AuthorityProxy authorityProxy = AuthorityProxyHelper.createProxy();
		authorityProxy.setAuthority("ROLE_USER");
		authorities.add(authorityProxy);
		userProxy.setAuthorities(authorities);
		userProxy.setFirstname("Test");
		userProxy.setLastname("Test");
		userProxy.setEmail("user@user.at");
		return userProxy;
	}
}
