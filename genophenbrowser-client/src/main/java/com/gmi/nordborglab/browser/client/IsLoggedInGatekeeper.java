package com.gmi.nordborglab.browser.client;

import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

public class IsLoggedInGatekeeper implements Gatekeeper {
	
	private final CurrentUser currentUser;
	
	@Inject
	public IsLoggedInGatekeeper(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}
	

	@Override
	public boolean canReveal() {
		return currentUser.isLoggedIn();
	}

}
