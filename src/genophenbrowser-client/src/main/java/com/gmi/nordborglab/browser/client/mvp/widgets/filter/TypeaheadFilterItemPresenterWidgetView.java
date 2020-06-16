package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.gmi.nordborglab.browser.client.util.TypeaheadUtils;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Suggestion;
import org.gwtbootstrap3.extras.typeahead.client.base.SuggestionCallback;
import org.gwtbootstrap3.extras.typeahead.client.events.TypeaheadSelectedEvent;
import org.gwtbootstrap3.extras.typeahead.client.events.TypeaheadSelectedHandler;
import org.gwtbootstrap3.extras.typeahead.client.ui.Typeahead;

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

    @UiField(provided = true)
    org.gwtbootstrap3.extras.typeahead.client.ui.Typeahead searchTa;
    @UiField
    DivElement filterName;
    @UiField
    DivElement controlContainer;

    private final Widget widget;

    private Suggestion<SearchItemProxy> currentSuggestion;

    @Inject
    public TypeaheadFilterItemPresenterWidgetView(Binder binder) {
        searchTa = new Typeahead(new Dataset<SearchItemProxy>() {
            @Override
            public void findMatches(String query, SuggestionCallback<SearchItemProxy> suggestionCallback) {
                currentSuggestion = null;
                getUiHandlers().onSearchByQuery(query, new TypeaheadUtils(suggestionCallback));

            }
        });
        widget = binder.createAndBindUi(this);
        container.setHeight("250px");
        searchTa.addTypeaheadSelectedHandler(new TypeaheadSelectedHandler<SearchItemProxy>() {
            @Override
            public void onSelected(TypeaheadSelectedEvent<SearchItemProxy> typeaheadSelectedEvent) {
                currentSuggestion = typeaheadSelectedEvent.getSuggestion();
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
        searchTa.setPlaceholder("Search for a " + name + " to filter...");
    }

    @Override
    public HasText getSearchTb() {
        return searchTa;
    }

    @Override
    public String[] getSearchSelectedValue() {
        if (currentSuggestion == null && (searchTa.getValue() == null || searchTa.getValue().equals("")))
            return null;
        if (currentSuggestion != null) {
            return new String[]{currentSuggestion.getData().getReplacementText(), currentSuggestion.getData().getId()};
        }
        return new String[]{searchTa.getValue(), null};
    }

    @UiHandler("searchTa")
    public void onSearchTbChanged(KeyUpEvent event) {
        if (event.isAnyModifierKeyDown()) {
            currentSuggestion = null;
        }
    }
}