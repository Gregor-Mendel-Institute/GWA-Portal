package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.stats.DateStatHistogramFacet")
public interface DateStatHistogramFacetProxy extends ValueProxy {

    public static enum TYPE {study, phenotype, analysis}

    ;

    public List<DateStatHistogramProxy> getHistogram();

    public TYPE getType();

}
