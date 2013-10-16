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

public class PhenotypeSearchProcessor extends TermSearchProcessor {


    public PhenotypeSearchProcessor(String term) {
        super(term);
    }

    @Override
    public SearchRequestBuilder getSearchBuilder(
            SearchRequestBuilder searchRequest) {
        searchRequest
                .setHighlighterTagsSchema("styled")
                .addField("local_trait_name")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setTypes("phenotype")
                .setQuery(multiMatchQuery(term, "local_trait_name^3.5", "local_trait_name.partial^1.5", "trait_protocol", "to_accession", "eo_accession"))
                .addHighlightedField("local_trait_name", 100, 0)
                .addHighlightedField("local_trait_name.partial", 100, 0)
                .addHighlightedField("to_accession", 100, 0)
                .addHighlightedField("eo_accession", 100, 0)
                .addHighlightedField("trait_protocol", 80, 5)
                .setSize(10);
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
                if (hit.getHighlightFields()
                        .containsKey("local_trait_name"))
                    searchTitle = hit.getHighlightFields().get("local_trait_name").getFragments()[0].string();
                else if (hit.getHighlightFields().containsKey("local_trait_name.partial"))
                    searchTitle = hit.getHighlightFields().get("local_trait_name.partial").getFragments()[0].string();
                else
                    searchTitle = (String) hit.getFields().get("local_trait_name")
                            .getValue();
                searchAddText = (String) hit.getFields().get("local_trait_name").getValue();
                searchItems.add(new SearchItem(hit.getId(), searchTitle,
                        searchAddText, CATEGORY.DIVERSITY, SUB_CATEGORY.PHENOTYPE));
            }
            facetPage = new SearchFacetPage(searchItems, null,
                    response.getHits().getTotalHits(),
                    SUB_CATEGORY.PHENOTYPE);
        }
        return facetPage;
    }

}
