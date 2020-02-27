package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="org.springframework.security.acls.model.Permission")
public interface PermissionProxy extends ValueProxy {
	
	
	
	int getMask();
}
