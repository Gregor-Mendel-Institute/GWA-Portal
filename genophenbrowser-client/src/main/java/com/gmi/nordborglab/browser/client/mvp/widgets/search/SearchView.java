package com.gmi.nordborglab.browser.client.mvp.widgets.search;

import com.gmi.nordborglab.browser.client.ui.SearchSuggestDisplay;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle.SearchSuggestion;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.InputGroupAddon;
import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;

public class SearchView extends ViewWithUiHandlers<SearchUiHandlers> implements SearchPresenter.MyView {

    private final Widget widget;
    private int minCharSize = 3;
    @UiField(provided = true)
    org.gwtbootstrap3.client.ui.SuggestBox suggestBox;
    @UiField
    InputGroupAddon loadingIndicator;

    public interface Binder extends UiBinder<Widget, SearchView> {
    }

    @Inject
    public SearchView(final Binder binder) {
        suggestBox = new SuggestBox(new SearchSuggestOracle() {

            @Override
            public void requestSuggestions(Request request, Callback callback) {
                if (request.getQuery().length() >= minCharSize) {
                    setLoadingIndicatorVisible(true);
                    getUiHandlers().onRequestSearch(request, callback);
                }
            }
        }, new TextBox(), new SearchSuggestDisplay());
        widget = binder.createAndBindUi(this);
        setLoadingIndicatorVisible(false);
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
        suggestBox.getElement().setAttribute("placeholder", "Global search...");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setMinCharSize(int minCharSize) {
        this.minCharSize = minCharSize;
    }

    @Override
    public void setLoadingIndicatorVisible(boolean visible) {
        loadingIndicator.setColor(visible ? "GREEN" : null);
        loadingIndicator.setIcon(visible ? IconType.SPINNER : IconType.SEARCH);
        loadingIndicator.setIconSpin(visible);
    }
}
