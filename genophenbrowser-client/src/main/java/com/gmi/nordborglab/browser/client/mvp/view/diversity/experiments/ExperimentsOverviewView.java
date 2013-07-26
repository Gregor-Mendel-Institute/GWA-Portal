package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;


import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentsOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentsOverviewPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerColumn;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;

public class ExperimentsOverviewView extends ViewWithUiHandlers<ExperimentsOverviewUiHandlers> implements
        ExperimentsOverviewPresenter.MyView {


    public interface Binder extends UiBinder<Widget, ExperimentsOverviewView> {
    }

    private final Widget widget;
    private final PlaceManager placeManager;

    @UiField(provided = true)
    DataGrid<ExperimentProxy> table;
    @UiField
    CustomPager pager;
    @UiField
    NavLink navAll;
    @UiField
    NavLink navPrivate;
    @UiField
    NavLink navPublished;
    @UiField
    NavLink navRecent;
    @UiField
    TextBox searchBox;


    private final BiMap<ConstEnums.TABLE_FILTER, NavLink> navLinkMap;

    @Inject
    public ExperimentsOverviewView(final Binder binder,
                                   final PlaceManager placeManager, final CustomDataGridResources dataGridResources) {
        this.placeManager = placeManager;
        table = new DataGrid<ExperimentProxy>(50, dataGridResources, new EntityProxyKeyProvider<ExperimentProxy>());
        initCellTable();
        widget = binder.createAndBindUi(this);
        navLinkMap = ImmutableBiMap.<ConstEnums.TABLE_FILTER, NavLink>builder()
                .put(ConstEnums.TABLE_FILTER.ALL, navAll)
                .put(ConstEnums.TABLE_FILTER.PRIVATE, navPrivate)
                .put(ConstEnums.TABLE_FILTER.PUBLISHED, navPublished)
                .put(ConstEnums.TABLE_FILTER.RECENT, navRecent).build();
        pager.setDisplay(table);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    private void initCellTable() {

        table.addColumn(new ExperimentListDataGridColumns.TitleColumn(placeManager, new ParameterizedPlaceRequest(NameTokens.experiment)), "Name");
        table.addColumn(new ExperimentListDataGridColumns.DesignColumn(), "Design");
        table.addColumn(new OwnerColumn(), "Owner");
        table.addColumn(new AccessColumn(), "Access");

        table.setColumnWidth(2, 150, Style.Unit.PX);
        table.setColumnWidth(3, 150, Style.Unit.PX);

    }

    @Override
    public HasData<ExperimentProxy> getDisplay() {
        return table;
    }

    @Override
    public void setActiveNavLink(ConstEnums.TABLE_FILTER filter) {
        for (NavLink link : navLinkMap.values()) {
            link.setActive(false);
        }
        navLinkMap.get(filter).setActive(true);
    }

    @Override
    public void displayFacets(List<FacetProxy> facets) {
        if (facets == null)
            return;
        for (FacetProxy facet : facets) {
            ConstEnums.TABLE_FILTER type = ConstEnums.TABLE_FILTER.valueOf(facet.getName());
            String newTitle = getFilterTitleFromType(type) + " (" + facet.getTotal() + ")";
            navLinkMap.get(type).setText(newTitle);
        }
    }

    private String getFilterTitleFromType(ConstEnums.TABLE_FILTER filter) {
        switch (filter) {
            case ALL:
                return "All";
            case PRIVATE:
                return "My studies";
            case PUBLISHED:
                return "Published";
            case RECENT:
                return "Recent";
        }
        return "";
    }

    @UiHandler({"navAll", "navPrivate", "navPublished", "navRecent"})
    public void onNavClick(ClickEvent e) {
        IconAnchor iconAnchor = (IconAnchor) e.getSource();
        getUiHandlers().selectFilter(navLinkMap.inverse().get(iconAnchor.getParent()));
    }

    @UiHandler("searchBox")
    public void onKeyUpSearchBox(KeyUpEvent e) {
        if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || searchBox.getValue().equalsIgnoreCase("")) {
            getUiHandlers().updateSearchString(searchBox.getValue());
        }
    }

}
