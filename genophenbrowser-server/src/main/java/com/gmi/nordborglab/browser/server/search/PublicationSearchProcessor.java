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
 * Date: 5/14/13
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationSearchProcessor extends TermSearchProcessor {


    public PublicationSearchProcessor(String term) {
        super(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(SearchRequestBuilder searchRequest) {
        searchRequest = searchRequest.addField("title").setTypes("publication")
                .setHighlighterTagsSchema("styled")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(5)
                .setQuery(multiMatchQuery(term, "title^3.5", "title.partial^1.5", "author"))
                .addHighlightedField("title", 100, 0)
                .addHighlightedField("title.partial",100,0)
                .addHighlightedField("author", 100, 5);
        return searchRequest;
    }

    @Override
    public SearchFacetPage extractSearchFacetPage(SearchResponse response) {
        SearchFacetPage searchFacetPage = null;
        String searchTitle = "";
        String searchAddText = "";
        if (response.getHits().getTotalHits() > 0) {
            List<SearchItem> searchItems  = new ArrayList<SearchItem>();
            for (SearchHit hit : response.getHits()) {
                if (hit.getHighlightFields().containsKey("title"))
                    searchTitle = hit.getHighlightFields().get("title")
                            .getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("title.partial"))
                    searchTitle = hit.getHighlightFields().get("title.partial").getFragments()[0].string();
                else
                    searchTitle = (String)hit.getFields().get("author").getValue();
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, SearchItemProxy.CATEGORY.DIVERSITY, SearchItemProxy.SUB_CATEGORY.PUBLICATION));
            }
            searchFacetPage = new SearchFacetPage(searchItems, null,
                    response.getHits().getTotalHits(),
                    SearchItemProxy.SUB_CATEGORY.PUBLICATION);
        }
        return searchFacetPage;
    }
}
