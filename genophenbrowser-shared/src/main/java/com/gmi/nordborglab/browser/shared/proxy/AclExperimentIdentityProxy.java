package com.gmi.nordborglab.browser.shared.proxy;

import java.util.Set;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.acl.AclExperimentIdentity", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface AclExperimentIdentityProxy extends EntityProxy{
	Long getId();
	
	Set<AclExperimentEntryProxy> getEntries();
}
