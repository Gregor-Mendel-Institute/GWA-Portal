package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/28/13
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.SecureEntity", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface SecureEntityProxy extends EntityProxy {

    public boolean isPublic();

    public boolean isOwner();

    AccessControlEntryProxy getUserPermission();

    AppUserProxy getOwnerUser();
}
