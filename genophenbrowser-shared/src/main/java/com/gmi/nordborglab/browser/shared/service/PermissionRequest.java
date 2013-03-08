package com.gmi.nordborglab.browser.shared.service;


import com.gmi.nordborglab.browser.shared.proxy.*;
import com.google.web.bindery.requestfactory.shared.ExtraTypes;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.PermissionService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
@ExtraTypes({GWASResultProxy.class,ExperimentProxy.class})
public interface PermissionRequest extends RequestContext {

	public Request<CustomAclProxy> getPermissions(SecureEntityProxy experiment);
	public Request<CustomAclProxy> updatePermissions(SecureEntityProxy experiment,CustomAclProxy acl);
	public Request<SearchPermissionUserRoleProxy> searchUserAndRoles(String query);
    public Request<List<AppUserProxy>> findAllUsers();
	
}
