package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.acl.Authority")
public interface AuthorityProxy extends ValueProxy{
	String getAuthority();
	void setAuthority(String authority);
}
