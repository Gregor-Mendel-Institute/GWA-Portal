package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.acl.AclSid", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface AclSidProxy extends EntityProxy {
	Long getId();
	boolean getPrincipal();
	String getSid();
}
