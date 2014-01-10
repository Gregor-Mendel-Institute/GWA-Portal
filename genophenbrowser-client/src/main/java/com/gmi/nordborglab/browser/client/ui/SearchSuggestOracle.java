package com.gmi.nordborglab.browser.client.ui;

import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.google.gwt.user.client.ui.SuggestOracle;

public abstract class SearchSuggestOracle extends SuggestOracle {

    public static class SearchSuggestion implements Suggestion {

        private final SearchFacetPageProxy searchFacetPage;
        private final SearchItemProxy searchItem;

        public SearchSuggestion(SearchItemProxy searchItem, SearchFacetPageProxy searchFacetPage) {
            this.searchFacetPage = searchFacetPage;
            this.searchItem = searchItem;
        }

        @Override
        public String getDisplayString() {
            return searchItem.getDisplayText();
        }

        @Override
        public String getReplacementString() {
            return searchItem.getReplacementText();
        }

        public SUB_CATEGORY getCategory() {
            return searchItem.getSubCategory();
        }

        public String getId() {
            return searchItem.getId();
        }

        public SearchFacetPageProxy getSearchFacetPage() {
            return searchFacetPage;
        }
    }
}
