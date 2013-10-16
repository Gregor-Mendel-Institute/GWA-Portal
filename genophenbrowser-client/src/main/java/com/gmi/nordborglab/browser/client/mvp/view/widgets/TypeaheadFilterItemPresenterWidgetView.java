package com.gmi.nordborglab.browser.client.mvp.view.widgets;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.TypeaheadFilterItemPresenterWidget;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
public class TypeaheadFilterItemPresenterWidgetView extends FilterItemPresenterWidgetView<TypeaheadFilterItemPresenterWidget.MyView> implements TypeaheadFilterItemPresenterWidget.MyView {

    private int minCharSize = 0;

    interface Binder extends UiBinder<Widget, TypeaheadFilterItemPresenterWidgetView> {

    }

    @UiField
    TextBox searchTb;
    @UiField(provided = true)
    Typeahead searchTa;
    @UiField
    DivElement filterName;
    @UiField
    DivElement controlContainer;

    private final Widget widget;
    private SearchSuggestOracle searchSuggestOracle;

    private SearchSuggestOracle.SearchSuggestion currentSuggestion;

    @Inject
    public TypeaheadFilterItemPresenterWidgetView(Binder binder) {
        searchTa = new Typeahead(new SearchSuggestOracle() {
            @Override
            public void requestSuggestions(Request request, Callback callback) {
                currentSuggestion = null;
                getUiHandlers().onSearchByQuery(request, callback);
            }
        });
        widget = binder.createAndBindUi(this);
        container.setHeight("250px");
        searchTa.setUpdaterCallback(new Typeahead.UpdaterCallback() {
            @Override
            public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
                currentSuggestion = (SearchSuggestOracle.SearchSuggestion) selectedSuggestion;
                return currentSuggestion.getReplacementString();
            }
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setFilterName(String name) {
        filterName.setInnerText(name);
        searchTb.setPlaceholder("Search for a " + name + " to filter...");
    }

    @Override
    public HasText getSearchTb() {
        return searchTb;
    }

    @Override
    public String[] getSegetSelectedValue() {
        if (currentSuggestion == null && (searchTb.getValue() == null || searchTb.getValue().equals("")))
            return null;
        if (currentSuggestion != null) {
            return new String[]{currentSuggestion.getReplacementString(), currentSuggestion.getId()};
        }
        return new String[]{searchTb.getValue(), null};
    }

    @UiHandler("searchTb")
    public void onSearchTbChanged(KeyUpEvent event) {
        if (event.isAnyModifierKeyDown()) {
            currentSuggestion = null;
        }
    }
}