package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry")
public interface AccessControlEntryProxy extends ValueProxy {
	
	public static final int READ = 1 << 0;
	 public static final int WRITE = 1 << 1;
	 public static final int CREATE = 1 << 2;
	 public static final int DELETE = 1 << 3;
	 public static final int ADMINISTRATION = 1 << 4;
	 
	Long getId();
	 
	int getMask();
	void setMask(int mask);
	
	boolean getIsGranting();
	void setIsGranting(boolean isGranting);
	
	PermissionPrincipalProxy getPrincipal();
	void setPrincipal(PermissionPrincipalProxy principal);
	
}
