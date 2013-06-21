package com.gmi.nordborglab.browser.server.search;

import com.gmi.nordborglab.browser.server.domain.SearchItem;
import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeneSearchProcessor extends TermSearchProcessor {

    public GeneSearchProcessor(String term) {
        super(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(SearchRequestBuilder searchRequest) {
        searchRequest.addField("name").setTypes("gene")
                .setHighlighterTagsSchema("styled")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(5)
                .setQuery(multiMatchQuery(term, "name^3.5", "name.partial^1.5"))
                .addHighlightedField("name", 100, 0)
                .addHighlightedField("name.partial", 100, 0);
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
                if (hit.getHighlightFields().containsKey("name"))
                    searchTitle = hit.getHighlightFields().get("name")
                            .getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("name.partial"))
                    searchTitle = hit.getHighlightFields().get("name.partial").getFragments()[0].string();
                searchAddText = hit.getFields().get("name").getValue();
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, SearchItemProxy.CATEGORY.DIVERSITY, SearchItemProxy.SUB_CATEGORY.GENE));
            }
            facetPage = new SearchFacetPage(searchItems, null,
                    response.getHits().getTotalHits(),
                    SearchItemProxy.SUB_CATEGORY.GENE);
        }
        return facetPage;
    }
}
