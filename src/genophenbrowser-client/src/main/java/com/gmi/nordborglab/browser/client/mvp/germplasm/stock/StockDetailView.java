package com.gmi.nordborglab.browser.client.mvp.germplasm.stock;

import com.gmi.nordborglab.browser.client.editors.StockDisplayEditor;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.DataView;
import com.googlecode.gwt.charts.client.RowFilter;
import com.googlecode.gwt.charts.client.orgchart.OrgChart;
import com.googlecode.gwt.charts.client.orgchart.OrgChartOptions;
import com.googlecode.gwt.charts.client.orgchart.OrgChartSize;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class StockDetailView extends ViewImpl implements
        StockDetailPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StockDetailView> {
    }

    public interface StockDisplayDriver extends RequestFactoryEditorDriver<StockProxy, StockDisplayEditor> {
    }

    private final ScheduledCommand layoutCmd = new ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };

    @UiField
    StockDisplayEditor stockDisplayEditor;
    @UiField
    OrgChart descendentsChart;
    @UiField
    OrgChart ancestorsChart;

    private DataTable pedigreeData = null;
    private boolean layoutScheduled = false;
    private final StockDisplayDriver displayDriver;
    private final PlaceManager placeManager;

    @Inject
    public StockDetailView(final Binder binder, final StockDisplayDriver displayDriver, final PlaceManager placeManager) {
        widget = binder.createAndBindUi(this);
        this.displayDriver = displayDriver;
        this.placeManager = placeManager;
        this.displayDriver.initialize(stockDisplayEditor);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setPedigreeData(DataTable pedigreeData) {
        this.pedigreeData = pedigreeData;
        formatDataTable();
        forceLayout();
    }

    private void drawPedigreeChart() {
        if (pedigreeData == null)
            return;
        OrgChartOptions options = OrgChartOptions.create();
        options.setAllowHtml(true);
        options.setSize(OrgChartSize.LARGE);
        options.setAllowCollapse(true);
        DataView ancestorsView = DataView.create(pedigreeData);
        DataView descendentsView = DataView.create(pedigreeData);

        ancestorsView.setRows(pedigreeData.getFilteredRows(getFilters(0)).cast());
        descendentsView.setRows(pedigreeData.getFilteredRows(getFilters(1)).cast());
        ancestorsChart.draw(ancestorsView, options);
        descendentsChart.draw(descendentsView, options);
    }

    private void forceLayout() {
        if (!widget.isAttached() || !widget.isVisible())
            return;
        drawPedigreeChart();
    }

    @Override
    public void scheduleLayout() {
        if (widget.isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    private JsArray<RowFilter> getFilters(int value) {
        RowFilter filter = RowFilter.createObject().cast();
        filter.setColumn(3);
        filter.setValue(value);
        JsArray<RowFilter> filters = JsArray.createArray().cast();
        filters.push(filter);
        return filters;
    }

    @Override
    public StockDisplayDriver getDisplayDriver() {
        return displayDriver;
    }

    private void formatDataTable() {
        if (pedigreeData == null)
            return;
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.stock);
        String formattedText = "";
        for (int i = 0; i < pedigreeData.getNumberOfRows(); i++) {
            String id = pedigreeData.getValueString(i, 0);
            String role = pedigreeData.getValueString(i, 2);
            request = request.with("id", id);
            formattedText = "<a href='#" + placeManager.buildHistoryToken(request.build()) + "'>" + id + "</a><br>" + role;
            pedigreeData.setFormattedValue(i, 0, formattedText);
        }
    }
}
