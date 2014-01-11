package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.PhenotypeListDataGridColumns;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerLinkColumn;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.List;

public class PhenotypeOverviewView extends ViewWithUiHandlers<PhenotypeOverviewUiHandlers> implements
        PhenotypeOverviewPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, PhenotypeOverviewView> {
    }

    @UiField(provided = true)
    DataGrid<PhenotypeProxy> dataGrid;
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
    private final PlaceManager placeManager;
    private final BiMap<ConstEnums.TABLE_FILTER, NavLink> navLinkMap;

    @Inject
    public PhenotypeOverviewView(final Binder binder,
                                 final PlaceManager placeManager,
                                 final CustomDataGridResources customDataGridResources) {
        this.placeManager = placeManager;
        dataGrid = new DataGrid<PhenotypeProxy>(20, customDataGridResources, new EntityProxyKeyProvider<PhenotypeProxy>());
        initGrid();
        widget = binder.createAndBindUi(this);
        navLinkMap = ImmutableBiMap.<ConstEnums.TABLE_FILTER, NavLink>builder()
                .put(ConstEnums.TABLE_FILTER.ALL, navAll)
                .put(ConstEnums.TABLE_FILTER.PRIVATE, navPrivate)
                .put(ConstEnums.TABLE_FILTER.PUBLISHED, navPublished)
                .put(ConstEnums.TABLE_FILTER.RECENT, navRecent).build();
        pager.setDisplay(dataGrid);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    private void initGrid() {
        dataGrid.setWidth("100%");
        dataGrid.setEmptyTableWidget(new Label("No Records found"));

        dataGrid.addColumn(new PhenotypeListDataGridColumns.TitleColumn(placeManager, new ParameterizedPlaceRequest(NameTokens.phenotype)), "Name");

        dataGrid.addColumn(new PhenotypeListDataGridColumns.ExperimentColumn(), "Experiment");
        dataGrid.addColumn(
                new PhenotypeListDataGridColumns.TraitOntologyColumn(),
                "Trait-Ontology");
        dataGrid.addColumn(new PhenotypeListDataGridColumns.EnvironOntologyColumn(), "Env-Ontology");
        dataGrid.addColumn(new PhenotypeListDataGridColumns.ProtocolColumn(),
                "Protocol");
        dataGrid.addColumn(new OwnerLinkColumn(placeManager), "Owner");
        dataGrid.addColumn(new AccessColumn(), "Access");

        dataGrid.setColumnWidth(0, 15, Unit.PCT);
        dataGrid.setColumnWidth(1, 15, Unit.PCT);
        dataGrid.setColumnWidth(2, 15, Unit.PCT);
        dataGrid.setColumnWidth(3, 15, Unit.PCT);
        dataGrid.setColumnWidth(4, 40, Unit.PCT);
        dataGrid.setColumnWidth(5, 100, Style.Unit.PX);
        dataGrid.setColumnWidth(6, 100, Style.Unit.PX);
    }

    @Override
    public HasData<PhenotypeProxy> getDisplay() {
        return dataGrid;
    }

    @Override
    public void setActiveNavLink(ConstEnums.TABLE_FILTER filter) {
        for (NavLink link : navLinkMap.values()) {
            link.setActive(false);
        }
        navLinkMap.get(filter).setActive(true);
    }

    @Override
    public void displayFacets(List<FacetProxy> facets, String searchString) {
        if (facets == null)
            return;
        for (FacetProxy facet : facets) {
            ConstEnums.TABLE_FILTER type = ConstEnums.TABLE_FILTER.valueOf(facet.getName());
            String newTitle = getFilterTitleFromType(type) + " (" + facet.getTotal() + ")";
            NavLink link = navLinkMap.get(type);
            link.setText(newTitle);
            PlaceRequest request = PhenotypeOverviewPresenter.place;
            if (type != ConstEnums.TABLE_FILTER.ALL) {
                request = request.with("filter", type.name());
            }
            if (searchString != null) {
                request = request.with("query", searchString);
            }
            searchBox.setText(searchString);
            link.setTargetHistoryToken(placeManager.buildHistoryToken(request));
        }
    }

    private String getFilterTitleFromType(ConstEnums.TABLE_FILTER filter) {
        switch (filter) {
            case ALL:
                return "All";
            case PRIVATE:
                return "My phenotypes";
            case PUBLISHED:
                return "Published";
            case RECENT:
                return "Recent";
        }
        return "";
    }

    @UiHandler("searchBox")
    public void onKeyUpSearchBox(KeyUpEvent e) {
        if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || searchBox.getValue().equalsIgnoreCase("")) {
            getUiHandlers().updateSearchString(searchBox.getValue());
        }
    }
}
