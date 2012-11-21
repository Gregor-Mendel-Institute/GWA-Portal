package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;

public interface SearchService {

	public List<SearchFacetPage> searchByTerm(String term,CATEGORY category,SUB_CATEGORY subCategory);
}
