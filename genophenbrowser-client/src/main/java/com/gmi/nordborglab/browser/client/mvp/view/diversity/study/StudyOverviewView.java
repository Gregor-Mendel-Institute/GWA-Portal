package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.gmi.nordborglab.browser.client.mvp.handlers.StudyOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.StudyListDataGridColumns;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerLinkColumn;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.List;

public class StudyOverviewView extends ViewWithUiHandlers<StudyOverviewUiHandlers> implements
        StudyOverviewPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StudyOverviewView> {
    }

    @UiField(provided = true)
    DataGrid<StudyProxy> dataGrid;
    @UiField
    CustomPager pager;
    @UiField
    SimplePanel facetContainer;
    private final PlaceManager placeManager;

    @Inject
    public StudyOverviewView(final Binder binder, final PlaceManager placeManager,
                             final CustomDataGridResources dataGridResources) {
        this.placeManager = placeManager;
        dataGrid = new DataGrid<StudyProxy>(20, dataGridResources, new EntityProxyKeyProvider<StudyProxy>());
        initGrid();
        widget = binder.createAndBindUi(this);
        pager.setDisplay(dataGrid);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget) {
            facetContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    private void initGrid() {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.study);
        dataGrid.setWidth("100%");
        dataGrid.setEmptyTableWidget(new Label("No Records found"));
        dataGrid.addColumn(new StudyListDataGridColumns.TitleColumn(placeManager, request), "Name");
        dataGrid.addColumn(new StudyListDataGridColumns.ExperimentColumn(), "Study");
        dataGrid.addColumn(new StudyListDataGridColumns.PhenotypeColumn(), "Phenotype");
        dataGrid.addColumn(new StudyListDataGridColumns.AlleleAssayColumn(), "Genotype");
        dataGrid.addColumn(new StudyListDataGridColumns.ProtocolColumn(), "Protocol");
        dataGrid.addColumn(new StudyListDataGridColumns.TransformationColumn(), "Trans.");
        List<HasCell<StudyJobProxy, ?>> cells = Lists.newArrayList();
        cells.add(new StudyListDataGridColumns.StatusCell());
        cells.add(new StudyListDataGridColumns.ProgressCell());
        dataGrid.addColumn(new StudyListDataGridColumns.StatusColumn(cells), "Status");
        dataGrid.addColumn(new OwnerLinkColumn(placeManager), "Owner");
        dataGrid.addColumn(new AccessColumn(), "Access");
        dataGrid.setColumnWidth(0, 25, Unit.PCT);
        dataGrid.setColumnWidth(1, 25, Unit.PCT);
        dataGrid.setColumnWidth(2, 25, Unit.PCT);
        dataGrid.setColumnWidth(3, 25, Unit.PCT);
        dataGrid.setColumnWidth(4, 80, Unit.PX);
        dataGrid.setColumnWidth(5, 80, Unit.PX);
        dataGrid.setColumnWidth(6, 200, Unit.PX);
        dataGrid.setColumnWidth(7, 100, Style.Unit.PX);
        dataGrid.setColumnWidth(8, 100, Style.Unit.PX);

    }

    @Override
    public HasData<StudyProxy> getDisplay() {
        return dataGrid;
    }
}
