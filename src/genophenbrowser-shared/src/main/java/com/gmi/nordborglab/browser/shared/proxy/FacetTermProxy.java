package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.es.ESTermsFacet")
public interface FacetTermProxy extends ValueProxy {
    public String getTerm();

    public double getValue();
}
