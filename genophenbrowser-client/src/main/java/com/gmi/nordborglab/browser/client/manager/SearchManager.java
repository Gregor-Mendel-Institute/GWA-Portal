package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.SearchRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.Collection;

/**
 * Created by uemit.seren on 4/1/15.
 */
public class SearchManager extends RequestFactoryManager<SearchRequest> {

    public interface SearchCallback {
        public void onSearchReturned(Collection<SearchItemProxy> response, SearchItemProxy.SUB_CATEGORY category, long total);
    }

    @Inject
    public SearchManager(CustomRequestFactory rf) {
        super(rf);
    }

    @Override
    public SearchRequest getContext() {
        return rf.searchRequest();
    }


    public void searchGeneByTerm(String query, final SearchCallback callback) {
        getContext().searchGeneByTerm(query).fire(new Receiver<SearchFacetPageProxy>() {
            @Override
            public void onSuccess(SearchFacetPageProxy response) {
                if (response != null) {
                    callback.onSearchReturned(getSearchItemsFromResponse(response), response.getCategory(), response.getTotal());
                }
            }
        });
    }

    public void searchByFilter(String request, ConstEnums.FILTERS filter, final SearchCallback callback) {
        getContext().searchByFilter(request, filter).fire(new Receiver<SearchFacetPageProxy>() {
            @Override
            public void onSuccess(SearchFacetPageProxy response) {
                if (response != null) {
                    callback.onSearchReturned(getSearchItemsFromResponse(response), response.getCategory(), response.getTotal());
                }
            }
        });
    }

    private Collection<SearchItemProxy> getSearchItemsFromResponse(SearchFacetPageProxy response) {
        Collection<SearchItemProxy> suggestions = Lists.newArrayList();
        if (response != null) {
            for (SearchItemProxy searchItem : response.getContents()) {
                suggestions.add(searchItem);
            }
        }
        return suggestions;
    }


}
