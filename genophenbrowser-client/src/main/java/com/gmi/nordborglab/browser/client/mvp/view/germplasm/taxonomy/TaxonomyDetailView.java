package com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;
import com.gmi.nordborglab.browser.client.editors.TaxonomyDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.TaxonomyEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.TaxonomyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy.TaxonomyDetailPresenter;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.PieChartOptions;
import com.googlecode.gwt.charts.client.geochart.GeoChartOptions;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendAlignment;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomyDetailView extends ViewWithUiHandlers<TaxonomyDetailUiHandlers> implements
        TaxonomyDetailPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, TaxonomyDetailView> {
    }

    public interface TaxonomyDisplayDriver extends RequestFactoryEditorDriver<TaxonomyProxy, TaxonomyDisplayEditor> {
    }

    public interface TaxonomyEditDriver extends RequestFactoryEditorDriver<TaxonomyProxy, TaxonomyEditEditor> {
    }

    @UiField
    TaxonomyDisplayEditor taxonomyDisplayEditor;
    @UiField
    TaxonomyEditEditor taxonomyEditEditor;
    @UiField
    ToggleButton edit;
    @UiField
    ToggleButton save;
    @UiField
    Anchor cancel;
    @UiField
    com.googlecode.gwt.charts.client.corechart.PieChart stockChart;
    @UiField
    com.googlecode.gwt.charts.client.corechart.PieChart accessionTypeChart;
    @UiField
    com.googlecode.gwt.charts.client.corechart.PieChart genotypeChart;
    @UiField
    com.googlecode.gwt.charts.client.geochart.GeoChart geoChart;
    @UiField
    LayoutPanel container;

    protected State state = State.DISPLAYING;
    private final TaxonomyDisplayDriver displayDriver;
    private final TaxonomyEditDriver editDriver;
    private boolean layoutScheduled = false;
    private DataTable geoChartData;
    private DataTable alleleAssayData;
    private DataTable sampStatData;
    private DataTable stockGenerationData;

    private final ScheduledCommand layoutCmd = new ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };

    @Inject
    public TaxonomyDetailView(final Binder binder, final TaxonomyDisplayDriver displayDriver, final TaxonomyEditDriver editDriver) {
        widget = binder.createAndBindUi(this);
        this.displayDriver = displayDriver;
        this.editDriver = editDriver;
        this.displayDriver.initialize(taxonomyDisplayEditor);
        this.editDriver.initialize(taxonomyEditEditor);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public TaxonomyDisplayDriver getDisplayDriver() {
        return displayDriver;
    }

    @Override
    public TaxonomyEditDriver getEditDriver() {
        return editDriver;
    }

    @Override
    public void setState(State state, int permission) {
        this.state = state;
        taxonomyDisplayEditor.setVisible(state == State.DISPLAYING);
        taxonomyEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        edit.setVisible(state == State.DISPLAYING &&
                (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
    }


    @UiHandler("edit")
    public void onEdit(ClickEvent e) {
        if (state == State.DISPLAYING) {
            getUiHandlers().onEdit();
        }
    }

    @UiHandler("save")
    public void onSave(ClickEvent e) {
        if (state == State.EDITING) {
            getUiHandlers().onSave();
        }
    }

    @UiHandler("cancel")
    public void onCancel(ClickEvent e) {
        if (state == State.EDITING) {
            getUiHandlers().onCancel();
        }
    }

    @Override
    public void setGeoChartData(DataTable geoChartData) {
        this.geoChartData = geoChartData;

    }

    @Override
    public void setAlleleAssayData(DataTable alleleAssayData) {
        this.alleleAssayData = alleleAssayData;
    }

    @Override
    public void setSampStatData(DataTable sampStatData) {
        this.sampStatData = sampStatData;
    }

    @Override
    public void setStockGenerationData(DataTable stockGenerationData) {
        this.stockGenerationData = stockGenerationData;

    }

    @Override
    public void scheduleLayout() {
        if (widget.isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    private void forceLayout() {
        if (!widget.isAttached() || !widget.isVisible())
            return;
        container.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.AUTO);
        drawAlleleAssayChart();
        drawGeoChart();
        drawSampStatChart();
        drawStockDataChart();
    }

    private GeoChart.Options createGeoChartOptions() {
        GeoChart.Options options = GeoChart.Options.create();
        options.setTitle("Geographic distribution");
        options.set("keepAspectRatio", false);
        return options;
    }

    private void drawStockDataChart() {
        PieChartOptions options = PieChartOptions.create();
        Legend legend = Legend.create();
        legend.setAligment(LegendAlignment.START);
        legend.setPosition(LegendPosition.TOP);
        options.setLegend(legend);
        stockChart.draw(stockGenerationData, options);
    }

    private void drawGeoChart() {
        GeoChartOptions options = GeoChartOptions.create();
        geoChart.draw(geoChartData, options);
    }

    private void drawAlleleAssayChart() {
        PieChartOptions options = PieChartOptions.create();
        genotypeChart.draw(alleleAssayData, options);
    }

    private void drawSampStatChart() {
        PieChartOptions options = PieChartOptions.create();
        accessionTypeChart.draw(sampStatData, options);
    }


}
