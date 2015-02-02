package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created by uemit.seren on 1/28/15.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.GWASResultPage")
public interface GWASResultPageProxy extends ValueProxy {
    List<GWASResultProxy> getContents();

    int getNumber();

    long getTotalElements();

    int getTotalPages();

    List<FacetProxy> getFacets();
}

