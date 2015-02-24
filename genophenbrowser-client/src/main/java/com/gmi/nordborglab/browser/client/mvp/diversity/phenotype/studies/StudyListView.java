package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.studies;

import com.github.gwtbootstrap.client.ui.Button;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.List;

public class StudyListView extends ViewWithUiHandlers<StudyListUiHandlers> implements
        StudyListPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StudyListView> {
    }

    public static ProvidesKey<StudyProxy> KEY_PROVIDER = new ProvidesKey<StudyProxy>() {
        @Override
        public Object getKey(StudyProxy item) {
            if (item != null && item.getId() != null) {
                return item.getId();
            }
            return null;
        }
    };

    @UiField(provided = true)
    DataGrid<StudyProxy> dataGrid;
    @UiField
    CustomPager pager;
    @UiField
    Button newAnalysisBtn;
    @UiField
    SimplePanel facetContainer;
    private final PlaceManager placeManager;

    @Inject
    public StudyListView(final Binder binder, final PlaceManager placeManager, final CustomDataGridResources dataGridResources) {
        this.placeManager = placeManager;
        dataGrid = new DataGrid<StudyProxy>(20, dataGridResources, KEY_PROVIDER);
        initCellTable();
        widget = binder.createAndBindUi(this);
        pager.setDisplay(dataGrid);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public HasData<StudyProxy> getDisplay() {
        return dataGrid;
    }

    private void initCellTable() {
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.study);
        dataGrid.setEmptyTableWidget(new Label("No Records found"));
        dataGrid.addColumn(new StudyListDataGridColumns.TitleColumn(placeManager, request), "Title");
        dataGrid.addColumn(new StudyListDataGridColumns.ProtocolColumn(), "Protocol");
        dataGrid.addColumn(new StudyListDataGridColumns.TransformationColumn(), "Trans.");
        dataGrid.addColumn(new StudyListDataGridColumns.AlleleAssayColumn(), "Genotype");
        dataGrid.addColumn(new StudyListDataGridColumns.StudyDateColumn(), "Study date");
        List<HasCell<StudyJobProxy, ?>> cells = Lists.newArrayList();
        cells.add(new StudyListDataGridColumns.StatusCell());
        cells.add(new StudyListDataGridColumns.ProgressCell());
        dataGrid.addColumn(new StudyListDataGridColumns.StatusColumn(cells), "Status");
        dataGrid.addColumn(new OwnerLinkColumn(placeManager), "Owner");
        dataGrid.addColumn(new AccessColumn(), "Access");
        dataGrid.setColumnWidth(0, 50, Style.Unit.PCT);
        dataGrid.setColumnWidth(1, 80, Style.Unit.PX);
        dataGrid.setColumnWidth(2, 80, Style.Unit.PX);
        dataGrid.setColumnWidth(3, 50, Style.Unit.PCT);
        dataGrid.setColumnWidth(4, 150, Style.Unit.PX);
        dataGrid.setColumnWidth(5, 200, Style.Unit.PX);
        dataGrid.setColumnWidth(6, 100, Style.Unit.PX);
        dataGrid.setColumnWidth(7, 120, Style.Unit.PX);
    }

    @UiHandler("newAnalysisBtn")
    public void onNewStudy(ClickEvent e) {
        getUiHandlers().onNewStudy();
    }

    @Override
    public void showAddBtn(boolean showAdd) {
        // TODO fix this
        //newStudyBtn.setVisible(false);
        newAnalysisBtn.setVisible(showAdd);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget) {
            facetContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

}
