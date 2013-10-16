package com.gmi.nordborglab.browser.client.mvp.presenter.widgets;

import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.shared.dto.FilterItemValue;
import com.gmi.nordborglab.browser.shared.proxy.SearchFacetPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
public class TypeaheadFilterItemPresenterWidget extends FilterItemPresenterWidget<TypeaheadFilterItemPresenterWidget.MyView> {

    public interface MyView extends FilterItemPresenterWidget.MyView {

        void setFilterName(String name);

        HasText getSearchTb();

        String[] getSegetSelectedValue();
    }

    private final CustomRequestFactory rf;


    @Inject
    public TypeaheadFilterItemPresenterWidget(EventBus eventBus, MyView view, final CustomRequestFactory rf) {
        super(eventBus, view);
        this.getView().setUiHandlers(this);
        this.rf = rf;
    }

    @Override
    List<FilterItemValue> createFilterItemValue() {
        String[] value = getView().getSegetSelectedValue();
        if (value == null)
            return null;
        return Lists.newArrayList(new FilterItemValue(value[0], value[1]));
    }

    @Override
    void init() {
        getView().setFilterName(filterType.name());
    }

    @Override
    void reset() {
        getView().getSearchTb().setText("");
    }

    @Override
    public void onSearchByQuery(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
        rf.searchRequest().searchByFilter(request.getQuery(), filterType).fire(new Receiver<SearchFacetPageProxy>() {

            @Override
            public void onSuccess(SearchFacetPageProxy response) {
                SuggestOracle.Response searchResponse = new SuggestOracle.Response();
                Collection<SuggestOracle.Suggestion> suggestions = Lists.newArrayList();
                if (response != null) {
                    for (SearchItemProxy searchItem : response.getContents()) {
                        suggestions.add(new SearchSuggestOracle.SearchSuggestion(searchItem));
                    }
                }
                searchResponse.setSuggestions(suggestions);
                callback.onSuggestionsReady(request, searchResponse);
            }
        });
    }
}