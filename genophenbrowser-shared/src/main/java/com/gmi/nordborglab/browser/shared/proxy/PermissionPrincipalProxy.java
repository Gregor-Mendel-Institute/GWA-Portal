package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.acl.PermissionPrincipal")
public interface PermissionPrincipalProxy extends ValueProxy {

	public String getId();
	public void setId(String id);
	
	public String getName();
	public void setName(String name);
	
	public boolean getIsUser();
	public void setIsUser(boolean isUser);
}
