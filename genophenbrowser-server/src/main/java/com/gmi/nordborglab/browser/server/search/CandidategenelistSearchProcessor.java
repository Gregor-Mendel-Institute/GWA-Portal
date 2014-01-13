package com.gmi.nordborglab.browser.server.search;

import com.gmi.nordborglab.browser.server.domain.SearchItem;
import com.gmi.nordborglab.browser.server.domain.pages.SearchFacetPage;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 16.10.13
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class CandidategenelistSearchProcessor extends TermSearchProcessor {

    public CandidategenelistSearchProcessor(String term) {
        super(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(SearchRequestBuilder searchRequest) {
        searchRequest = searchRequest.addField("name").setTypes("candidate_gene_list")
                .setHighlighterTagsSchema("styled")
                .setFrom(0)
                .setSize(5)
                .setQuery(multiMatchQuery(term, "name^3.5", "name.partial^1.5", "description^0.5"))
                .addHighlightedField("name", 100, 0)
                .addHighlightedField("name.partial", 100, 0);
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
                        searchAddText, SearchItemProxy.CATEGORY.DIVERSITY, SearchItemProxy.SUB_CATEGORY.CANDIDATE_GENE_LIST));
            }
            searchFacetPage = new SearchFacetPage(searchItems, null,
                    response.getHits().getTotalHits(),
                    SearchItemProxy.SUB_CATEGORY.CANDIDATE_GENE_LIST);
        }
        return searchFacetPage;
    }
}
