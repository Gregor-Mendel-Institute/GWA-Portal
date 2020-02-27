package com.gmi.nordborglab.browser.server.search;

import com.gmi.nordborglab.browser.server.domain.SearchItem;
import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.CATEGORY;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.idsQuery;

public class StockSearchProcessor extends TermSearchProcessor {


    public StockSearchProcessor(String term) {
        super(term);
    }

    @Override
    protected QueryBuilder getQuery() {
        return idsQuery("stock").ids(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(
            SearchRequestBuilder searchRequest) {
        searchRequest.addFields("_id", "generation.comments")
                .setTypes("stock")
                .setHighlighterTagsSchema("styled")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(5)
                .setQuery(getFilteredQueryBuilder());
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
                searchTitle = (String) hit.getId() + " [" + (String) hit.getFields().get("generation.comments").getValue() + "]";
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, CATEGORY.GERMPLASM, SUB_CATEGORY.STOCK));
            }
            facetPage = new SearchFacetPage(searchItems, null, response
                    .getHits().getTotalHits(), SUB_CATEGORY.STOCK);
        }
        return facetPage;
    }

}
