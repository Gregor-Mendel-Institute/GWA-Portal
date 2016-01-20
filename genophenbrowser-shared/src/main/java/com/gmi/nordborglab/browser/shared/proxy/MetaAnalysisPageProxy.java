package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created by uemit.seren on 1/15/16.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.MetaAnalysisPage")
public interface MetaAnalysisPageProxy extends ValueProxy {

    List<MetaAnalysisProxy> getContents();

    int getNumber();

    long getTotalElements();

    int getTotalPages();

    int getMaxAssocCount();
}
