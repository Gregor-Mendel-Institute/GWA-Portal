package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 19.06.13
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage")
public interface MetaSNPAnalysisPageProxy extends ValueProxy {
    List<MetaSNPAnalysisProxy> getContent();

    int getNumber();

    long getTotalElements();

    int getTotalPages();
}


