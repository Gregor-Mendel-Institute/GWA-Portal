package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.AppUserPage")
public interface AppUserPageProxy extends ValueProxy {
    List<AppUserProxy> getContents();

    int getNumber();

    long getTotalElements();

    int getTotalPages();

    List<FacetProxy> getFacets();
}
