package com.gmi.nordborglab.browser.client.mvp.widgets.facets;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.NavPills;
import org.gwtbootstrap3.client.ui.TextBox;

import java.util.Map;

/**
 * Created by uemit.seren on 2/10/15.
 */
public class FacetSearchPresenterWidgetView extends ViewWithUiHandlers<FacetSearchUiHandler> implements FacetSearchPresenterWidget.MyView {

    @UiField
    NavPills navPills;
    @UiField
    TextBox searchBox;
    @UiField
    InputGroup searchBoxContainer;

    public interface Binder extends UiBinder<Widget, FacetSearchPresenterWidgetView> {

    }

    private Map<String, AnchorListItem> navLinkMap;

    @Inject
    public FacetSearchPresenterWidgetView(Binder binder) {
        initWidget(binder.createAndBindUi(this));
        navLinkMap = Maps.newHashMap();
    }


    @Override
    public void clearFacets() {
        navLinkMap.clear();
        navPills.clear();
    }

    @Override
    public void setFacet(String name, String title, String historyToken) {
        AnchorListItem link = navLinkMap.get(name);
        if (link == null) {
            link = new AnchorListItem();
            navLinkMap.put(name, link);
            navPills.add(link);
        }
        link.setText(title);
        link.setTargetHistoryToken(historyToken);
    }

    @Override
    public void setSearchString(String searchString) {
        searchBox.setText(searchString);
    }

    @Override
    public void setActiveNavLink(String facet) {
        for (AnchorListItem link : navLinkMap.values()) {
            link.setActive(false);
        }
        if (navLinkMap.containsKey(facet))
            navLinkMap.get(facet).setActive(true);
    }

    @Override
    public void setSearchBoxVisible(boolean visible) {
        searchBoxContainer.setVisible(visible);
    }

    @UiHandler("searchBox")
    public void onKeyUpSearchBox(KeyUpEvent e) {
        if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || searchBox.getValue().equalsIgnoreCase("")) {
            getUiHandlers().updateSearchString(searchBox.getValue());
        }
    }
}