package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.Set;

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.acl.AclExperimentIdentity", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface AclExperimentIdentityProxy extends EntityProxy {
    Long getId();

    Set<AclExperimentEntryProxy> getEntries();
}
