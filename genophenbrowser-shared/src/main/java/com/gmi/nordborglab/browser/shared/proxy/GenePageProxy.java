package com.gmi.nordborglab.browser.shared.proxy;

import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.09.13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.GenePage")
public interface GenePageProxy extends ValueProxy {

    List<GeneProxy> getContent();

    int getNumber();

    long getTotalElements();

    int getTotalPages();

    List<FacetProxy> getFacets();

    List<FacetProxy> getStatsFacets();
}
