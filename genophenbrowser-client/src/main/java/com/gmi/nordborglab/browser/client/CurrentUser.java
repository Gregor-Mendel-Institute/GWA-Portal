package com.gmi.nordborglab.browser.client;

import javax.annotation.Nullable;

import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;


public class CurrentUser{
	
	private AppUserProxy appUser = null;
	private AppDataProxy appData = null;
	
	public CurrentUser() {}
	
	public void setAppUser(AppUserProxy appuser) {
		this.appUser = appuser;
	}
	
	public boolean isLoggedIn() {
		return appUser != null;
	}
	
	public AppUserProxy getAppUser()  {
		return appUser;
	}
	
	public int getPermissionMask(AccessControlEntryProxy ace) {
		int permission = 0;
		if (isLoggedIn()) {
			if (ace != null ) {
				permission = ace.getMask();
			}
		}
		return permission;
	}

	public void setAppData(AppDataProxy appData) {
		this.appData = appData;
	    addNullValues();
	}
	
	
	public AppDataProxy getAppData() {
		return appData;
	}
	
	private void addNullValues() {
		if (appData == null)
			return;
		appData.getUnitOfMeasureList().add(0, null);
		appData.getStatisticTypeList().add(0, null);
		appData.getStudyProtocolList().add(0,null);
		appData.getAlleleAssayList().add(0,null);
	}

	public boolean isAdmin() {
		if (!isLoggedIn())
			return false;
		if (appUser.getAuthorities() == null)
			return false;
		AuthorityProxy authority = Iterables.find(appUser.getAuthorities(), new Predicate<AuthorityProxy>() {

			@Override
			public boolean apply(@Nullable AuthorityProxy input) {
				if (input == null)
					return false;
				if (input.getAuthority().equals("ROLE_ADMIN"))
					return true;
				return false;
			}
		});
		if (authority == null)
			return false;
		return true;
	}
}
