package com.gmi.nordborglab.browser.server.search;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.SearchHit;

import com.gmi.nordborglab.browser.server.domain.SearchItem;
import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;

public class PassportSearchProcessor extends TermSearchProcessor {
	
	
	public PassportSearchProcessor(String term) {
		super(term);
	}

	@Override
	public SearchRequestBuilder getSearchBuilder(
			SearchRequestBuilder searchRequest) {
		searchRequest.addFields("accename")
		.setTypes("passport")
		.setHighlighterTagsSchema("styled")
		.setSearchType(SearchType.QUERY_THEN_FETCH)
		.setFrom(0)
		.setSize(10)
		.setQuery(multiMatchQuery(term,"_id^50.0","accename","accename.partial","accenumb","comments"))
		.addHighlightedField("accename", 100, 0)
		.addHighlightedField("accename.partial",100,0)
		.addHighlightedField("accenumb", 100, 3)
		.addHighlightedField("comments", 150, 3);
	return searchRequest;
	}

	@Override
	public SearchFacetPage extractSearchFacetPage(SearchResponse response) {
		SearchFacetPage facetPage = null;
		String searchTitle = "";
		String searchAddText = "";
		if (response.getHits().getTotalHits() > 0) {
			List<SearchItem> searchItems = new ArrayList<SearchItem>();
			for (SearchHit hit : response.getHits()) {
				if (hit.getHighlightFields().containsKey("accename"))
					searchTitle = hit.getHighlightFields()
							.get("accename").getFragments()[0].string();
				else if (hit.getHighlightFields().containsKey("accename.partial"))
					searchTitle = hit.getHighlightFields().get("accename.partial").getFragments()[0].string();
				else
					searchTitle = (String)hit.getFields().get("accename").getValue();
				searchTitle = searchTitle + " [" + (String)hit.getId()+"]";
				searchItems.add(new SearchItem(hit.getId(), searchTitle,
						searchAddText, CATEGORY.GERMPLASM, SUB_CATEGORY.PASSPORT));
			}
			facetPage = new SearchFacetPage(searchItems, null, response
							.getHits().getTotalHits(), SUB_CATEGORY.PASSPORT);
		}
		return facetPage;
	}

}
