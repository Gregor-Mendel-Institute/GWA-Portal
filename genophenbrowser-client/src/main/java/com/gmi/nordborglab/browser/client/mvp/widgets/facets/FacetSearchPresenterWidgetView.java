package com.gmi.nordborglab.browser.client.mvp.widgets.facets;

import com.github.gwtbootstrap.client.ui.InputAddOn;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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
    InputAddOn searchBoxContainer;

    public interface Binder extends UiBinder<Widget, FacetSearchPresenterWidgetView> {

    }

    private Map<String, NavLink> navLinkMap;

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
        NavLink link = navLinkMap.get(name);
        if (link == null) {
            link = new NavLink();
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
        for (NavLink link : navLinkMap.values()) {
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