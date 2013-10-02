package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 23.09.13
 * Time: 13:19
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.CandidateGeneListPage")
public interface CandidateGeneListPageProxy extends ValueProxy {

    List<CandidateGeneListProxy> getContent();

    int getNumber();

    long getTotalElements();

    int getTotalPages();

    List<FacetProxy> getFacets();

}
