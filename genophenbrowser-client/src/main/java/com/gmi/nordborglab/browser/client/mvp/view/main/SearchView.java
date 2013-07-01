package com.gmi.nordborglab.browser.client.mvp.view.main;

import com.github.gwtbootstrap.client.ui.base.TextBox;
import com.gmi.nordborglab.browser.client.mvp.handlers.SearchUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.SearchPresenter;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestDisplay;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle.SearchSuggestion;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Callback;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class SearchView extends ViewWithUiHandlers<SearchUiHandlers> implements SearchPresenter.MyView {

    private final Widget widget;
    private int minCharSize = 3;
    @UiField(provided = true)
    SuggestBox suggestBox;

    public interface Binder extends UiBinder<Widget, SearchView> {
    }

    @Inject
    public SearchView(final Binder binder) {
        suggestBox = new SuggestBox(new SearchSuggestOracle() {

            @Override
            public void requestSuggestions(Request request, Callback callback) {
                if (request.getQuery().length() >= minCharSize)
                    getUiHandlers().onRequestSearch(request, callback);
            }
        }, new TextBox(), new SearchSuggestDisplay());
        widget = binder.createAndBindUi(this);
        suggestBox.addSelectionHandler(new SelectionHandler<Suggestion>() {

            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                if (event.getSelectedItem() == null)
                    return;
                SearchSuggestion searchSuggestion = (SearchSuggestion) event.getSelectedItem();
                suggestBox.setText(null);
                getUiHandlers().onNavigateToSuggestion(searchSuggestion);
            }
        });
        // for TOur
        suggestBox.getElement().setId("globalSearchBox");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setMinCharSize(int minCharSize) {
        this.minCharSize = minCharSize;
    }
}
