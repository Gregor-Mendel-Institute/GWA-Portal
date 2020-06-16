package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created by uemit.seren on 3/4/15.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.SNPInfoPage")
public interface SNPInfoPageProxy extends ValueProxy {
    List<SNPInfoProxy> getContents();

    int getNumber();

    long getTotalElements();

    int getTotalPages();

    List<FacetProxy> getFacets();
}


