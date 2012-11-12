package com.gmi.nordborglab.browser.shared.proxy;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole")
public interface SearchPermissionUserRoleProxy extends ValueProxy {

	public List<PermissionPrincipalProxy> getPrincipals();
}
