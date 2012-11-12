package com.gmi.nordborglab.browser.server.security;

import org.springframework.security.acls.domain.BasePermission;

public class CustomPermission extends BasePermission {

	public CustomPermission(int mask) {
		super(mask);
	}

}
