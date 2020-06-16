package com.gmi.nordborglab.browser.client.mvp.widgets.facets;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by uemit.seren on 2/10/15.
 */
public class FacetSearchPresenterWidget extends PresenterWidget<FacetSearchPresenterWidget.MyView> implements FacetSearchUiHandler {


    public interface MyView extends View, HasUiHandlers<FacetSearchUiHandler> {

        void clearFacets();

        void setFacet(String name, String title, String historyToken);

        void setSearchString(String searchString);

        void setActiveNavLink(String facet);

        void setSearchBoxVisible(boolean visible);
    }

    public static final PermanentSlot<FacetSearchPresenterWidget> SLOT_CONTENT = new PermanentSlot<>();
    private String searchString = null;
    private String defaultFilter = "PRIVATE";
    private String filter = "PRIVATE";
    private String defaultFilterParam = "filter";
    private String defaultQueryParam = "query";
    private final PlaceManager placeManager;
    private Map<String, String> fixedFacets;

    public static final Map<String, String> SHARED_MAP = ImmutableMap.<String, String>builder()
            .put(ConstEnums.TABLE_FILTER.ALL.name(), "All")
            .put(ConstEnums.TABLE_FILTER.PRIVATE.name(), "My items")
            .put(ConstEnums.TABLE_FILTER.SHARED.name(), "Shared")
            .put(ConstEnums.TABLE_FILTER.RECENT.name(), "Recent")
            .build();

    public static final ImmutableMap<String, String> STANDARD_MAP = ImmutableMap.<String, String>builder()
            .put(ConstEnums.TABLE_FILTER.ALL.name(), "All")
            .put(ConstEnums.TABLE_FILTER.PRIVATE.name(), "My items")
            .put(ConstEnums.TABLE_FILTER.PUBLISHED.name(), "Published")
            .put(ConstEnums.TABLE_FILTER.RECENT.name(), "Recent")
            .build();

    public static final ImmutableMap<String, String> USER_MAP = ImmutableMap.<String, String>builder()
            .put(ConstEnums.USER_FILTER.ALL.name(), "All")
            .put(ConstEnums.USER_FILTER.USER.name(), "Users")
            .put(ConstEnums.USER_FILTER.ADMIN.name(), "Admins")
            .build();


    @Inject
    public FacetSearchPresenterWidget(EventBus eventBus, FacetSearchPresenterWidget.MyView view, PlaceManager placeManager) {
        super(eventBus, view);
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        initFixedFacets(STANDARD_MAP);
    }

    @Override
    public void updateSearchString(String searchString) {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        placeManager.revealPlace(new PlaceRequest.Builder(request).with(defaultQueryParam, searchString).build());
    }

    @Override
    protected void onReset() {
        super.onReset();
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        String newFilter = request.getParameter(defaultFilterParam, defaultFilter);
        String newSearchString = request.getParameter(defaultQueryParam, null);
        if (!isDynamic() && !fixedFacets.containsKey(newFilter))
            return;
        boolean isFilterChanged = newFilter != filter;
        boolean isSearchStringChanged = newSearchString != searchString;
        if (isFilterChanged || isSearchStringChanged) {
            filter = newFilter;
            searchString = newSearchString;
            getEventBus().fireEventFromSource(new FacetSearchChangeEvent(isFilterChanged, isSearchStringChanged), this);
        }
        getView().setActiveNavLink(filter);
        getView().setSearchString(searchString);
    }

    public void displayFacets(List<FacetProxy> facets) {
        for (FacetProxy facet : facets) {
            if (!isDynamic() && !fixedFacets.containsKey(facet.getName()))
                continue;
            String title = getTitle(facet);
            String historyToken = getHistoryToken(facet.getName());
            getView().setFacet(facet.getName(), title, historyToken);
        }
        getView().setActiveNavLink(filter);
    }

    public void initFixedFacets(Map<String, String> fixedFacets) {
        if (fixedFacets == null)
            fixedFacets = ImmutableMap.copyOf(Collections.EMPTY_MAP);
        this.fixedFacets = fixedFacets;
        getView().clearFacets();
        for (Map.Entry<String, String> entry : fixedFacets.entrySet()) {
            getView().setFacet(entry.getKey(), entry.getValue(), getHistoryToken(entry.getKey()));
        }
        getView().setActiveNavLink(filter);
    }

    private String getTitle(FacetProxy facet) {
        String title = facet.getName();
        if (!isDynamic() && fixedFacets.containsKey(facet.getName()))
            title = fixedFacets.get(title);
        return title + " (" + facet.getTotal() + ")";
    }

    private String getHistoryToken(String facet) {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        if (request == null)
            return "";
        return placeManager.buildHistoryToken(new PlaceRequest.Builder(request).with(defaultFilterParam, facet).build());
    }

    public void setSearchBoxVisible(boolean visible) {
        getView().setSearchBoxVisible(visible);
    }

    public String getSearchString() {
        return searchString;
    }

    public String getFilter() {
        return filter;
    }

    public boolean isDynamic() {
        return fixedFacets.size() == 0;
    }

    public void setDefaultFilter(String defaultFilter) {
        this.defaultFilter = defaultFilter;
        this.filter = defaultFilter;
    }

    public void setDefaultFilterParam(String defaultFilterParam) {
        this.defaultFilterParam = defaultFilterParam;
    }

    public void setDefaultQueryParam(String defaultQueryParam) {
        this.defaultQueryParam = defaultQueryParam;
    }

    public void setFilter(String filter) {
        PlaceRequest request = placeManager.getCurrentPlaceRequest();
        placeManager.revealPlace(new PlaceRequest.Builder(request).with(defaultFilterParam, filter).build());
    }
}
