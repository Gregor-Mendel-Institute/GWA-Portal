package com.gmi.nordborglab.browser.client.mvp.diversity.meta.topsnps;

import com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes.MetaAnalysisGeneView;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.inject.Inject;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.Selection;
import com.googlecode.gwt.charts.client.corechart.PieChart;
import com.googlecode.gwt.charts.client.corechart.PieChartOptions;
import com.googlecode.gwt.charts.client.event.ReadyEvent;
import com.googlecode.gwt.charts.client.event.ReadyHandler;
import com.googlecode.gwt.charts.client.event.SelectEvent;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.Slice;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisTopResultsView extends ViewWithUiHandlers<MetaAnalysisTopResultsUiHandlers> implements
        MetaAnalysisTopResultsPresenter.MyView {

    interface Binder extends UiBinder<Widget, MetaAnalysisTopResultsView> {
    }

    private final Widget widget;
    @UiField
    SimpleLayoutPanel chartContainer1;
    @UiField
    SimpleLayoutPanel chartContainer2;
    @UiField
    SimpleLayoutPanel chartContainer3;
    @UiField
    SimpleLayoutPanel chartContainer4;
    @UiField
    PieChart chrPieChart;
    @UiField
    PieChart inGenePieChart;
    @UiField
    PieChart overFDRPieChart;
    @UiField
    PieChart annotationPieChart;
    @UiField(provided = true)
    DataGrid<MetaSNPAnalysisProxy> dataGrid;
    @UiField
    CustomPager pager;
    @UiField
    SimplePanel filterContainer;

    BiMap<MetaAnalysisTopResultsPresenter.STATS, PieChart> stats2Chart;
    Map<MetaAnalysisTopResultsPresenter.STATS, DataTable> stats2DataTable;
    Map<MetaAnalysisTopResultsPresenter.STATS, Selection[]> stats2Selection;

    private List<MetaAnalysisTopResultsPresenter.STATS> chartsToUpdate;
    private boolean layoutScheduled = false;
    private final PlaceManager placeManger;

    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };
    private Slice greySliceOption = Slice.create();
    //private DataTable emptyDataTable;


    public class ChartSelectHandler extends com.googlecode.gwt.charts.client.event.SelectHandler {

        private final MetaAnalysisTopResultsPresenter.STATS stat;

        public ChartSelectHandler(MetaAnalysisTopResultsPresenter.STATS stat) {
            this.stat = stat;
        }

        @Override
        public void onSelect(SelectEvent selectEvent) {
            PieChart chart = stats2Chart.get(stat);
            JsArray<Selection> sel = chart.getSelection();
            // FIXME because of https://code.google.com/p/gwt-charts/issues/detail?id=52
            if (sel.length() == 0) {
                chart.setSelection();
            }
            // FIXME because chart.getSelections return jsarray we have to convert
            Selection[] selections = new Selection[sel.length()];
            for (int i = 0; i < sel.length(); i++) {
                selections[i] = sel.get(i);
            }
            greyOutSlices(stat, selections);
            Integer row = null;
            if (selections.length > 0) {
                row = selections[0].getRow();
            }
            getUiHandlers().onChangeSelections(stat, row);
        }
    }

    public class ChartReadyHandler extends ReadyHandler {

        private final MetaAnalysisTopResultsPresenter.STATS stat;

        public ChartReadyHandler(MetaAnalysisTopResultsPresenter.STATS stat) {
            this.stat = stat;
        }

        @Override
        public void onReady(ReadyEvent readyEvent) {
            final Selection[] selections = stats2Selection.get(stat);
            PieChart chart = stats2Chart.get(stat);
            if (selections != null && selections.length != 0) {
                chart.setSelection(selections);
            }
        }
    }

    @Inject
    public MetaAnalysisTopResultsView(Binder binder, final PlaceManager placeManger,
                                      final CustomDataGridResources customDataGridResources) {
        this.placeManger = placeManger;
        dataGrid = new DataGrid<>(25, customDataGridResources);
        stats2DataTable = Maps.newHashMap();
        stats2Selection = Maps.newHashMap();
        greySliceOption.setColor("#eee");
        initDataGrid();
        widget = binder.createAndBindUi(this);
        stats2Chart = ImmutableBiMap.<MetaAnalysisTopResultsPresenter.STATS, PieChart>builder().put(MetaAnalysisTopResultsPresenter.STATS.CHR, chrPieChart)
                .put(MetaAnalysisTopResultsPresenter.STATS.INGENE, inGenePieChart).put(MetaAnalysisTopResultsPresenter.STATS.MAF, overFDRPieChart)
                .put(MetaAnalysisTopResultsPresenter.STATS.ANNOTATION, annotationPieChart).build();
        initChartHandlers();
        pager.setDisplay(dataGrid);
    }

    private void initDataGrid() {
        dataGrid.setWidth("100%");
        dataGrid.setMinimumTableWidth(1000, Style.Unit.PX);
        dataGrid.setEmptyTableWidget(new Label("No Records found"));
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.AnalysisColumn(placeManger), "Analysis");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.PhenotypeColumn(placeManger), "Phenotype");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.StudyColumn(placeManger), "Study");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.GenotypeColumn(), "Genotype");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MethodColumn(), "Method");
        dataGrid.setColumnWidth(4, 80, Style.Unit.PX);
        dataGrid.addColumn(new IdentityColumn<MetaSNPAnalysisProxy>(new MetaAnalysisGeneView.ScoreCell()), "pVal");
        dataGrid.setColumnWidth(5, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MafColumn(), "Maf");
        dataGrid.setColumnWidth(6, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MacColumn(), "Mac");
        dataGrid.setColumnWidth(7, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.ChrColumn(), "Chr");
        dataGrid.setColumnWidth(8, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.SNPColumn(), "SNP");
        dataGrid.setColumnWidth(9, 100, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.GeneColumn(placeManger), "Gene");
        dataGrid.setColumnWidth(10, 120, Style.Unit.PX);
    }

    private void initChartHandlers() {
        for (Map.Entry<MetaAnalysisTopResultsPresenter.STATS, PieChart> entry : stats2Chart.entrySet()) {
            PieChart chart = entry.getValue();
            MetaAnalysisTopResultsPresenter.STATS stat = entry.getKey();
            chart.addSelectHandler(new ChartSelectHandler(stat));
            chart.addReadyHandler(new ChartReadyHandler(stat));
        }
    }


    private void greyOutSlices(MetaAnalysisTopResultsPresenter.STATS stat, Selection[] selections) {
        stats2Selection.put(stat, selections);
        PieChart pieChart = stats2Chart.get(stat);
        PieChartOptions options = getPieOptions(getTitleFromStat(stat), false);
        DataTable dataTable = stats2DataTable.get(stat);
        Slice[] sliceOptions = getGreySlicesFromSelection(dataTable, selections);
        options.setSlices(sliceOptions);
        pieChart.draw(dataTable, options);
    }

    private Slice[] getGreySlicesFromSelection(DataTable dataTable, Selection[] selections) {
        if (selections.length == 0) {
            return new Slice[]{};
        }
        Slice[] options = new Slice[dataTable.getNumberOfRows()];
        for (int i = 0; i < dataTable.getNumberOfRows(); i++) {
            Slice sliceOption = Slice.create();
            if (i != selections[0].getRow()) {
                sliceOption = greySliceOption;
            }
            options[i] = sliceOption;
        }
        return options;
    }

    private DataTable createEmptyDataTable(String term) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, "stats");
        dataTable.addColumn(ColumnType.NUMBER, "count");
        dataTable.addRows(1);
        dataTable.setValue(0, 0, term);
        dataTable.setValue(0, 1, 1);
        return dataTable;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    private PieChartOptions getPieOptions(String title, boolean isBlank) {
        PieChartOptions options = PieChartOptions.create();
        options.setTitle(title);
        if (isBlank) {
            options.setColors("#eee");
        }
        Animation animation = Animation.create();
        Options animationOptions = Options.create();
        return options;
    }

    @Override
    public void setStatsData(DataTable dataTable, MetaAnalysisTopResultsPresenter.STATS stat) {
        stats2DataTable.put(stat, dataTable);
    }

    @Override
    public void scheduleLayout() {
        if (widget.isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    @Override
    public void resetSelection(List<MetaAnalysisTopResultsPresenter.STATS> stats) {
        if (stats == null) {
            for (PieChart chart : stats2Chart.values()) {
                chart.setSelection(new Selection[]{});
            }
            stats2Selection.clear();
        } else {
            for (MetaAnalysisTopResultsPresenter.STATS stat : stats) {
                PieChart chart = stats2Chart.get(stat);
                chart.setSelection(new Selection[]{});
                stats2Selection.put(stat, null);
            }
        }

    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MetaAnalysisTopResultsPresenter.TYPE_FilterContent) {
            filterContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    @Override
    public HasData<MetaSNPAnalysisProxy> getDisplay() {
        return dataGrid;
    }

    @Override
    public void setPagingDisabled(boolean disabled) {
        pager.setPageControlDisabled(disabled);
    }

    private void forceLayout() {
        if (!widget.isAttached() || !widget.isVisible())
            return;
        drawCharts();
    }

    private void drawCharts() {
        for (Map.Entry<MetaAnalysisTopResultsPresenter.STATS, PieChart> entry : stats2Chart.entrySet()) {
            if (stats2DataTable.containsKey(entry.getKey())) {
                Selection[] selections = stats2Selection.get(entry.getKey());
                if (selections == null || selections.length == 0) {
                    PieChart chart = entry.getValue();
                    entry.getValue().draw(stats2DataTable.get(entry.getKey()), getPieOptions(getTitleFromStat(entry.getKey()), false));
                }
            }
        }
    }

    private String getTitleFromStat(MetaAnalysisTopResultsPresenter.STATS stat) {
        String title = "";
        switch (stat) {
            case CHR:
                title = "Chromosomes";
                break;
            case INGENE:
                title = "SNP region";
                break;
            case OVERFDR:
                title = "SNP FDR";
                break;
            case MAF:
                title = "MAF";
                break;
            case ANNOTATION:
                title = "SNP annotation";
                break;
        }
        return title;
    }

}