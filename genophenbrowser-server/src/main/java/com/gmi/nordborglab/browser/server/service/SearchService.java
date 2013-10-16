package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;

import java.util.List;

public interface SearchService {

    public List<SearchFacetPage> searchByTerm(String term, CATEGORY category, SUB_CATEGORY subCategory);

    public SearchFacetPage searchGeneByTerm(String term);

    public SearchFacetPage searchByFilter(String query, ConstEnums.FILTERS filter);
}
