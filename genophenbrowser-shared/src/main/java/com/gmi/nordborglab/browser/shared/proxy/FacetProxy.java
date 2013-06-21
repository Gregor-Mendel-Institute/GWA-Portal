package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.es.ESFacet")
public interface FacetProxy extends ValueProxy {

    public String getName();

    public long getMissing();

    public long getTotal();

    public long getOther();

    public List<FacetTermProxy> getTerms();

}
