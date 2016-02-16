package com.gmi.nordborglab.browser.client.mvp.diversity.phenotypes;

import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes.PhenotypeListDataGridColumns;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerLinkColumn;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

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
    SimplePanel facetContainer;
    private final PlaceManager placeManager;

    @Inject
    public PhenotypeOverviewView(final Binder binder,
                                 final PlaceManager placeManager,
                                 final CustomDataGridResources customDataGridResources) {
        this.placeManager = placeManager;
        dataGrid = new DataGrid<>(25, customDataGridResources, new EntityProxyKeyProvider<PhenotypeProxy>());
        initGrid();
        widget = binder.createAndBindUi(this);
        bindSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetContainer);
        pager.setDisplay(dataGrid);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    private void initGrid() {
        dataGrid.setWidth("100%");
        dataGrid.setEmptyTableWidget(new Label("No Records found"));

        dataGrid.addColumn(new PhenotypeListDataGridColumns.TitleColumn(placeManager, new PlaceRequest.Builder().nameToken(NameTokens.phenotype)), "Name");

        dataGrid.addColumn(new PhenotypeListDataGridColumns.ExperimentColumn(), "Study");
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
}
