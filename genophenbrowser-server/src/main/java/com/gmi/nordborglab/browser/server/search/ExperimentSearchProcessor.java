package com.gmi.nordborglab.browser.server.search;

import static org.elasticsearch.index.query.QueryBuilders.*;

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

public class ExperimentSearchProcessor extends TermSearchProcessor {


    public ExperimentSearchProcessor(String term) {
        super(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(SearchRequestBuilder searchRequest) {
        searchRequest = searchRequest.addField("name").setTypes("experiment")
                .setHighlighterTagsSchema("styled")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(5)
                .setQuery(multiMatchQuery(term, "name^3.5", "name.partial^1.5", "originator", "design", "comments^0.5"))
                .addHighlightedField("name", 100, 0)
                .addHighlightedField("name.partial", 100, 0)
                .addHighlightedField("originator", 100, 5)
                .addHighlightedField("design", 150, 5)
                .addHighlightedField("comments", 150, 5);
        return searchRequest;
    }

    @Override
    public SearchFacetPage extractSearchFacetPage(SearchResponse response) {
        SearchFacetPage searchFacetPage = null;
        String searchTitle = "";
        String searchAddText = "";
        if (response.getHits().getTotalHits() > 0) {
            List<SearchItem> searchItems = new ArrayList<SearchItem>();
            for (SearchHit hit : response.getHits()) {
                if (hit.getHighlightFields().containsKey("name"))
                    searchTitle = hit.getHighlightFields().get("name")
                            .getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("name.partial"))
                    searchTitle = hit.getHighlightFields().get("name.partial").getFragments()[0].string();
                else
                    searchTitle = (String) hit.getFields().get("name").getValue();
                searchAddText = (String) hit.getFields().get("name").getValue();
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, CATEGORY.DIVERSITY, SUB_CATEGORY.STUDY));
            }
            searchFacetPage = new SearchFacetPage(searchItems, null,
                    response.getHits().getTotalHits(),
                    SUB_CATEGORY.STUDY);
        }
        return searchFacetPage;
    }

}
