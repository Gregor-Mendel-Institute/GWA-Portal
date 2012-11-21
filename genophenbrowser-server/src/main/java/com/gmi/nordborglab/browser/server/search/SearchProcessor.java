package com.gmi.nordborglab.browser.server.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;

import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;

public interface SearchProcessor {
	public SearchRequestBuilder getSearchBuilder(SearchRequestBuilder searchRequest);
	public SearchFacetPage extractSearchFacetPage(SearchResponse response);
}
