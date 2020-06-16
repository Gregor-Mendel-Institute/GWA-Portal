package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole")
public interface SearchPermissionUserRoleProxy extends ValueProxy {

    public List<PermissionPrincipalProxy> getPrincipals();
}
