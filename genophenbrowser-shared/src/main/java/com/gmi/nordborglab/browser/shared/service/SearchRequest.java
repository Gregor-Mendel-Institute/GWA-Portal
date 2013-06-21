package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.SearchService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface SearchRequest extends RequestContext {

    public Request<List<SearchFacetPageProxy>> searchByTerm(String term, CATEGORY category, SUB_CATEGORY subCategory);

    Request<SearchFacetPageProxy> searchGeneByTerm(String query);
}
