package com.gmi.nordborglab.browser.shared.proxy;


import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.acl.AclExperimentEntry", locator="com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface AclExperimentEntryProxy extends EntityProxy{
	
	    
	Long getId();
	
	Integer getMask();
	
	AclSidProxy getSid();
}
