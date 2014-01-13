package com.gmi.nordborglab.browser.shared.service;


import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchPermissionUserRoleProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.web.bindery.requestfactory.shared.ExtraTypes;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.PermissionService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
@ExtraTypes({GWASResultProxy.class, ExperimentProxy.class, PhenotypeProxy.class, StudyProxy.class, CandidateGeneListProxy.class})
public interface PermissionRequest extends RequestContext {

    public Request<CustomAclProxy> getPermissions(SecureEntityProxy experiment);

    public Request<CustomAclProxy> updatePermissions(SecureEntityProxy experiment, CustomAclProxy acl);

    public Request<SearchPermissionUserRoleProxy> searchUserAndRoles(String query);

    public Request<List<AppUserProxy>> findAllUsers();

}
