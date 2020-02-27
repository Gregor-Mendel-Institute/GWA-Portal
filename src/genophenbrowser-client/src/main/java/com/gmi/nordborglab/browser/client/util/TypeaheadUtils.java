package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.client.manager.SearchManager;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.common.collect.Lists;
import org.gwtbootstrap3.extras.typeahead.client.base.Suggestion;
import org.gwtbootstrap3.extras.typeahead.client.base.SuggestionCallback;

import java.util.Collection;

/**
 * Created by uemit.seren on 4/1/15.
 */
public class TypeaheadUtils implements SearchManager.SearchCallback {

    final SuggestionCallback callback;

    public TypeaheadUtils(SuggestionCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onSearchReturned(Collection<SearchItemProxy> response, SearchItemProxy.SUB_CATEGORY category, long total) {
        Collection<Suggestion<SearchItemProxy>> suggestions = Lists.newArrayList();
        if (response != null) {
            for (SearchItemProxy searchItem : response) {
                suggestions.add(Suggestion.create(searchItem.getReplacementText(), searchItem, null));
            }
        }
        callback.execute(suggestions);
    }

}
