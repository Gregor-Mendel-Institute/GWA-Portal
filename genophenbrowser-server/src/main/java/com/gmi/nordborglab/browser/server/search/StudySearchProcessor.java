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

public class StudySearchProcessor extends TermSearchProcessor {


    public StudySearchProcessor(String term) {
        super(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(
            SearchRequestBuilder searchRequest) {
        searchRequest = searchRequest
                .setHighlighterTagsSchema("styled")
                .setFrom(0)
                .setSize(5)
                .setTypes("study")
                .addFields("name", "protocol.analysis_method",
                        "alelele_assay.name")
                .setSearchType(SearchType.QUERY_THEN_FETCH).setTypes("study")
                .setQuery(multiMatchQuery(term, "name^3.5", "name.partial^1.5"))
                .addHighlightedField("name", 100, 0)
                .addHighlightedField("name.partial", 100, 0);
        return searchRequest;
    }

    @Override
    public SearchFacetPage extractSearchFacetPage(SearchResponse response) {
        // Get results from study
        SearchFacetPage facetPage = null;
        String searchTitle = "";
        String searchAddText = "";
        if (response.getHits().getTotalHits() > 0) {
            List<SearchItem> searchItems = new ArrayList<SearchItem>();
            for (SearchHit hit : response.getHits()) {
                if (hit.getHighlightFields().containsKey("name"))
                    searchTitle = hit.getHighlightFields()
                            .get("name").getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("name.partial"))
                    searchTitle = hit.getHighlightFields().get("name.partial").getFragments()[0].string();
                else
                    searchTitle = (String) hit.getFields().get("name").getValue();
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, CATEGORY.DIVERSITY, SUB_CATEGORY.ANALYSIS));
            }
            facetPage = new SearchFacetPage(searchItems, null, response
                    .getHits().getTotalHits(), SUB_CATEGORY.ANALYSIS);
        }
        return facetPage;
    }

}
