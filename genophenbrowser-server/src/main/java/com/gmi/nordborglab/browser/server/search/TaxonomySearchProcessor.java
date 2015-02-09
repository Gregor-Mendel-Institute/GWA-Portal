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

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

public class TaxonomySearchProcessor extends TermSearchProcessor {


    public TaxonomySearchProcessor(String term) {
        super(term);
    }

    @Override
    protected QueryBuilder getQuery() {
        return multiMatchQuery(term, "genus", "genus.partial", "species", "species.partial", "subspecies", "subspecies.partial", "subtaxa", "subtaxa.partial", "common_name", "common_name.partial", "race", "population");
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(
            SearchRequestBuilder searchRequest) {
        searchRequest.addFields("genus", "species")
                .setTypes("taxonomy")
                .setHighlighterTagsSchema("styled")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(5)
                .setQuery(getFilteredQueryBuilder())
                .addHighlightedField("genus", 100, 0)
                .addHighlightedField("genus.partial", 100, 0)
                .addHighlightedField("species", 100, 0)
                .addHighlightedField("species.partial", 150, 0);
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
                if (hit.getHighlightFields().containsKey("genus"))
                    searchTitle = hit.getHighlightFields()
                            .get("genus").getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("genus.partial"))
                    searchTitle = hit.getHighlightFields().get("genus.partial").getFragments()[0].string();
                else
                    searchTitle = (String) hit.getFields().get("genus").getValue();
                searchTitle = searchTitle + " ";
                if (hit.getHighlightFields().containsKey("species"))
                    searchTitle += hit.getHighlightFields()
                            .get("species").getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("species.partial"))
                    searchTitle += hit.getHighlightFields().get("species.partial").getFragments()[0].string();
                else
                    searchTitle += (String) hit.getFields().get("species").getValue();
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, CATEGORY.GERMPLASM, SUB_CATEGORY.TAXONOMY));
            }
            facetPage = new SearchFacetPage(searchItems, null, response
                    .getHits().getTotalHits(), SUB_CATEGORY.TAXONOMY);
        }
        return facetPage;
    }

}
