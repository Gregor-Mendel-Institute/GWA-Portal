package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.github.gwtbootstrap.client.ui.ListBox;
import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.client.mvp.handlers.SNPDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.SNPDetailPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.SNPAnnotProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPGWASInfoProxy;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.primitives.Doubles;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HRElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.inject.Inject;
import com.googlecode.gwt.charts.client.ChartLayoutInterface;
import com.googlecode.gwt.charts.client.corechart.CandlestickChart;
import com.googlecode.gwt.charts.client.corechart.CandlestickChartOptions;
import com.googlecode.gwt.charts.client.corechart.ScatterChart;
import com.googlecode.gwt.charts.client.corechart.ScatterChartOptions;
import com.googlecode.gwt.charts.client.event.ReadyEvent;
import com.googlecode.gwt.charts.client.event.ReadyHandler;
import com.googlecode.gwt.charts.client.options.Bar;
import com.googlecode.gwt.charts.client.options.HAxis;
import com.googlecode.gwt.charts.client.options.TextPosition;
import com.googlecode.gwt.charts.client.options.VAxis;
import com.googlecode.gwt.charts.client.options.ViewWindowMode;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.google.gwt.query.client.GQuery.$;


public class SNPDetailView
        extends ViewWithUiHandlers<SNPDetailUiHandlers> implements SNPDetailPresenter.MyView {


    //TODO requrired because of https://code.google.com/p/gwt-charts/issues/detail?id=57&thanks=57&ts=1416306888
    static class ChartLayoutInterfaceExt extends ChartLayoutInterface {
        protected ChartLayoutInterfaceExt() {
        }

        public final native int getXLocation(double position)/*-{
            return this.getXLocation(position);
        }-*/;

        public final native int getXLocation(double position, int axisIndex) /*-{
            return this.getXLocation(position, axisIndex);
        }-*/;

        public final native double getYLocation(double position) /*-{
            return this.getYLocation(position);
        }-*/;

        public final native int getYLocation(double position, int axisIndex) /*-{
            return this.getYLocation(position, axisIndex);
        }-*/;

    }

    public interface MyStyle extends CssResource {
        public String meanLine();

        String vizContainer();

        String snpinfo();
    }

    interface Binder extends UiBinder<Widget, SNPDetailView> {

    }

    public enum CHART_TYPE {
        table, explorer, boxplot;
    }

    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };

    @UiField(provided = true)
    MainResources mainRes;


    @UiField
    LayoutPanel vizContainer;

    @UiField
    SpanElement chrLb;
    @UiField
    SpanElement posLb;
    @UiField
    SpanElement scoreLb;
    @UiField
    SpanElement typeLb;
    @UiField
    SpanElement geneLb;
    @UiField
    SimpleLayoutPanel lowerChartContainer;

    @UiField
    MyStyle style;

    @UiField
    HTMLPanel tableChartBtnContainer;
    @UiField
    HTMLPanel motionChartBtnContainer;
    @UiField
    HTMLPanel boxplotChartBtnContainer;
    @UiField
    DivElement refAlleleBar;
    @UiField
    SpanElement refAlleleLb;
    @UiField
    DivElement altAlleleBar;
    @UiField
    SpanElement altAlleleLb;
    private LayoutPanel boxPlotStripContainer = new LayoutPanel();
    private FlowPanel countryContainer = new FlowPanel();
    private ListBox countryFilter = new ListBox();
    private List<SNPAllele> snpAlleles;

    SNPAnnotProxy alleleInfo;
    private com.googlecode.gwt.charts.client.DataTable stripChartData;
    private ScatterChart stripChart = new ScatterChart();
    private CHART_TYPE chartType = CHART_TYPE.table;
    private boolean layoutScheduled = false;
    private DataGrid<SNPAllele> alleleDataGrid;
    private ResizeableMotionChart motionChart;
    private DataTable explorerData;
    private com.googlecode.gwt.charts.client.DataTable boxPlotData;
    private CandlestickChart candlestickChart = new CandlestickChart();
    private final SNPDetailDataGridColumns.PhenotypeBarHasCell phenotypeBarHasCell = new SNPDetailDataGridColumns.PhenotypeBarHasCell();
    private HRElement candlestickChartRefAlleleMeanLine = DOM.createElement("hr").cast();
    private HRElement candlestickChartAltAlleleMeanLine = DOM.createElement("hr").cast();
    private HRElement stripChartRefAlleleMeanLine = DOM.createElement("hr").cast();
    private HRElement stripChartAltAlleleMeanLine = DOM.createElement("hr").cast();
    private ImmutableMultimap<String, SNPAllele> allele2SnpAllele;
    private double candleStickBoxWidth = 1;
    private boolean resizeRefAllele = false;
    private double refMeanValue;
    private double altMeanValue;
    private ColumnSortEvent.ListHandler<SNPAllele> sortHandler = new ColumnSortEvent.ListHandler<>(null);

    private static final String COLOR_REF = "#3366cc";
    private static final String COLOR_ALT = "#dc3912";

    @Inject
    SNPDetailView(final Binder binder, final CustomDataGridResources customDataGridResources,
                  final FlagMap flagMap, final MainResources mainRes) {
        this.mainRes = mainRes;
        alleleDataGrid = new DataGrid<>(Integer.MAX_VALUE, customDataGridResources);
        initDataGrid(flagMap);
        initWidget(binder.createAndBindUi(this));
        vizContainer.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        vizContainer.getElement().getFirstChildElement().getNextSiblingElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        boxPlotStripContainer.add(candlestickChart);
        boxPlotStripContainer.setWidgetLeftWidth(candlestickChart, 0, Style.Unit.PCT, 50, Style.Unit.PCT);
        boxPlotStripContainer.add(stripChart);
        boxPlotStripContainer.setWidgetRightWidth(stripChart, 0, Style.Unit.PCT, 50, Style.Unit.PCT);
        candlestickChartRefAlleleMeanLine.setClassName(style.meanLine());
        candlestickChartAltAlleleMeanLine.setClassName(style.meanLine());
        stripChartRefAlleleMeanLine.setClassName(style.meanLine());
        stripChartAltAlleleMeanLine.setClassName(style.meanLine());
        refAlleleBar.getStyle().setBackgroundColor(COLOR_REF);
        altAlleleBar.getStyle().setBackgroundColor(COLOR_ALT);
        candlestickChart.addReadyHandler(new ReadyHandler() {
            @Override
            public void onReady(ReadyEvent readyEvent) {
                sizeCandleStickBoxes();
                drawCandleStickMeanLines();
            }
        });

        stripChart.addReadyHandler(new ReadyHandler() {
            @Override
            public void onReady(ReadyEvent event) {
                drawStripMeanLines();
            }
        });
        countryContainer.add(countryFilter);
        boxPlotStripContainer.add(countryContainer);
        boxPlotStripContainer.setWidgetVerticalPosition(countryContainer, Layout.Alignment.BEGIN);
        countryFilter.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                filterChartData();
                candlestickChart.draw(boxPlotData, createCandleStickOptions());
                stripChart.draw(stripChartData, createStripChartOptions());
            }
        });

    }


    private void initDataGrid(FlagMap flagMap) {

        alleleDataGrid.setWidth("100%");
        alleleDataGrid.setMinimumTableWidth(1000, Style.Unit.PX);
        alleleDataGrid.setEmptyTableWidget(new Label("No Records found"));
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.RowIDColumn(), "#");
        alleleDataGrid.setColumnWidth(0, 60, Style.Unit.PX);
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.IDColumn(), "ID");
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.NameColumn(), "Name");
        List<HasCell<SNPAllele, ?>> cells = Lists.newArrayList();
        cells.add(phenotypeBarHasCell);
        cells.add(new SNPDetailDataGridColumns.PhenotypeHasCell());
        alleleDataGrid.addColumn(new IdentityColumn<SNPAllele>(new SNPDetailDataGridColumns.PhenotypeCell(cells)), new TextHeader("Phenotype"));
        alleleDataGrid.getColumn(3).setSortable(true);
        sortHandler.setComparator(alleleDataGrid.getColumn(3), new Comparator<SNPAllele>() {
            @Override
            public int compare(SNPAllele o1, SNPAllele o2) {
                Double v1 = Double.valueOf(o1.getPhenotype());
                Double v2 = Double.valueOf(o2.getPhenotype());
                return Doubles.compare(v1, v2);
            }
        });
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.LongitudeColumn(), "Lon");
        alleleDataGrid.setColumnWidth(4, 80, Style.Unit.PX);
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.LatitudeColumn(), "Lat");
        alleleDataGrid.setColumnWidth(5, 80, Style.Unit.PX);
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.CountryColumn(flagMap), "Country");
        alleleDataGrid.setColumnWidth(6, 80, Style.Unit.PX);
        alleleDataGrid.addColumn(new SNPDetailDataGridColumns.AlleleColumn(), "Allele");
        alleleDataGrid.setColumnWidth(7, 60, Style.Unit.PX);
        alleleDataGrid.getColumn(7).setSortable(true);
        sortHandler.setComparator(alleleDataGrid.getColumn(7), new Comparator<SNPAllele>() {
            @Override
            public int compare(SNPAllele o1, SNPAllele o2) {
                if (o1 == o2) {
                    return 0;
                }

                // Compare the name columns.
                if (o1 != null) {
                    return (o2 != null) ? o1.getAllele().compareTo(o2.getAllele()) : 1;
                }
                return -1;
            }
        });
        alleleDataGrid.addColumnSortHandler(sortHandler);
    }

    private void showPanel() {
        lowerChartContainer.clear();
        switch (chartType) {
            case table:
                lowerChartContainer.add(alleleDataGrid);
                break;
            case explorer:
                motionChart = new ResizeableMotionChart(explorerData,
                        createMotionChartOptions());
                lowerChartContainer.add(motionChart);
                break;
            case boxplot:
                lowerChartContainer.add(boxPlotStripContainer);
                candlestickChart.draw(boxPlotData, createCandleStickOptions());
                stripChart.draw(stripChartData, createStripChartOptions());
                break;
        }
    }

    private void sizeCandleStickBoxes() {
        // use the color (red and blue) to change size
        GQuery rect = getCandleStickBar(resizeRefAllele);
        double x = Double.valueOf(rect.get(0).getAttribute("x"));
        double width = Double.valueOf(rect.get(0).getAttribute("width"));
        double scaledWidth = width * candleStickBoxWidth;
        rect.attr("width", scaledWidth);
        rect.attr("x", x + (width - scaledWidth) / 2);
    }

    private void drawStripMeanLines() {
        drawStripMeanLine(stripChartRefAlleleMeanLine, refMeanValue, 2.0);
        drawStripMeanLine(stripChartAltAlleleMeanLine, altMeanValue, 6.0);
    }


    private void drawStripMeanLine(HRElement element, double meanValue, double startPos) {
        ChartLayoutInterfaceExt layoutInterface = stripChart.getChartLayoutInterface().cast();
        ;

        double yPos = Math.round(layoutInterface.getYLocation(meanValue)) - 0.5;
        double left = Math.round((layoutInterface.getXLocation(startPos))) - 0.5;
        double right = Math.round((layoutInterface.getXLocation(startPos + 2.0)));

        element.getStyle().setLeft(left, Style.Unit.PX);
        element.getStyle().setWidth(right - left, Style.Unit.PX);
        element.getStyle().setTop(yPos, Style.Unit.PX);
        stripChart.getElement().getFirstChildElement().appendChild(element);
    }

    private void drawCandleStickMeanLines() {
        //draw ref candlestick
        GQuery rect = getCandleStickBar(true);
        drawCandleStickMeanLine(rect, candlestickChartRefAlleleMeanLine, refMeanValue);

        rect = getCandleStickBar(false);
        drawCandleStickMeanLine(rect, candlestickChartAltAlleleMeanLine, altMeanValue);
    }

    private void drawCandleStickMeanLine(GQuery rect, HRElement element, double meanValue) {
        ChartLayoutInterfaceExt layoutInterface = candlestickChart.getChartLayoutInterface().cast();
        ;
        Element bar = rect.get(0);
        double width = Math.round(Double.valueOf(bar.getAttribute("width")));
        double left = Math.round(Double.valueOf(bar.getAttribute("x")));
        double yPos = Math.round(layoutInterface.getYLocation(meanValue));
        double top = Double.valueOf(bar.getAttribute("y"));
        double height = Double.valueOf(bar.getAttribute("height"));
        width = width + 0.5;
        left = left - 0.5;

        element.getStyle().setLeft(left, Style.Unit.PX);
        element.getStyle().setWidth(width, Style.Unit.PX);
        element.getStyle().setTop(yPos, Style.Unit.PX);

        // mean line outside of box
        if (yPos >= top && yPos <= (top + height)) {
            element.getStyle().setBorderColor("white");
        } else {
            element.getStyle().clearBorderColor();
        }
        candlestickChart.getElement().getFirstChildElement().appendChild(element);
    }


    private double calculateMean(Collection<SNPAllele> snpAlleles) {
        double sum = 0;
        for (SNPAllele snpAllele : snpAlleles) {
            sum += Double.valueOf(snpAllele.getPhenotype());
        }
        return sum / snpAlleles.size();
    }

    private CandlestickChartOptions createCandleStickOptions() {
        CandlestickChartOptions options = CandlestickChartOptions.create();
        options.setTitle("Candlestickchart");
        options.setVAxis(VAxis.create("Phenotype"));
        options.setEnableInteractivity(false);
        Bar bar = Bar.create();
        options.setBar(bar);
        return options;
    }

    private ScatterChartOptions createStripChartOptions() {
        ScatterChartOptions options = ScatterChartOptions.create();
        options.setTitle("Stripchart");
        VAxis vAxis = VAxis.create("Phenotype");
        vAxis.setViewWindowMode(ViewWindowMode.MAXIMIZED);
        HAxis hAxis = HAxis.create();
        hAxis.setTitle(null);
        hAxis.setMinValue(0);
        hAxis.setMaxValue(10);
        hAxis.setTextPosition(TextPosition.NONE);
        options.setVAxis(vAxis);
        options.setHAxis(hAxis);
        return options;
    }

    private MotionChart.Options createMotionChartOptions() {
        MotionChart.Options options = MotionChart.Options.create();
        options.set(
                "state",
                "%7B%22time%22%3A%22notime%22%2C%22iconType%22%3A%22VBAR%22%2C%22xZoomedDataMin%22%3Anull%2C%22yZoomedDataMax%22%3Anull%2C%22xZoomedIn%22%3Afalse%2C%22iconKeySettings%22%3A%5B%5D%2C%22showTrails%22%3Atrue%2C%22xAxisOption%22%3A%224%22%2C%22colorOption%22%3A%227%22%2C%22yAxisOption%22%3A%224%22%2C%22playDuration%22%3A15%2C%22xZoomedDataMax%22%3Anull%2C%22orderedByX%22%3Afalse%2C%22duration%22%3A%7B%22multiplier%22%3A1%2C%22timeUnit%22%3A%22none%22%7D%2C%22xLambda%22%3A1%2C%22orderedByY%22%3Afalse%2C%22sizeOption%22%3A%22_UNISIZE%22%2C%22yZoomedDataMin%22%3Anull%2C%22nonSelectedAlpha%22%3A0.4%2C%22stateVersion%22%3A3%2C%22dimensions%22%3A%7B%22iconDimensions%22%3A%5B%22dim0%22%5D%7D%2C%22yLambda%22%3A1%2C%22yZoomedIn%22%3Afalse%7D%3B");
        options.setHeight(lowerChartContainer.getOffsetHeight());
        options.setWidth(lowerChartContainer.getOffsetWidth());
        return options;
    }

    @Override
    public HasData<SNPAllele> getSNPAlleleDisplay() {
        return alleleDataGrid;
    }

    private void forceLayout() {
        if (!asWidget().isAttached() || !asWidget().isVisible())
            return;
        showPanel();
    }

    @Override
    public void scheduledLayout() {
        if (asWidget().isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    @UiHandler("motionChartBtn")
    public void onMotionChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.explorer);
    }

    @UiHandler("tableChartBtn")
    public void onTableChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.table);
    }

    @UiHandler("boxplotChartBtn")
    public void onBoxplotChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.boxplot);
    }

    private void updateChartType(CHART_TYPE newChartType) {
        if (this.chartType == newChartType)
            return;
        this.chartType = newChartType;

        tableChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        motionChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        boxplotChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        switch (this.chartType) {
            case explorer:
                motionChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
            case table:
                tableChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
            case boxplot:
                boxplotChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
        }

        showPanel();
    }

    @Override
    public void setExplorerData(List<SNPAllele> snpAlleles) {
        this.snpAlleles = snpAlleles;
        ImmutableListMultimap<String, SNPAllele> countries = Multimaps.index(snpAlleles, new Function<SNPAllele, String>() {
            @Nullable
            @Override
            public String apply(@Nullable SNPAllele input) {
                return input.getPassport().getCollection().getLocality().getCountry();
            }
        });
        countryFilter.clear();
        countryFilter.addItem("Worldwide (" + snpAlleles.size() + ")", "");
        for (String country : Ordering.natural().immutableSortedCopy(countries.keySet())) {
            countryFilter.addItem(country + " (" + countries.get(country).size() + ")", country);
        }
        filterChartData();
    }

    private void filterChartData() {
        Collection<SNPAllele> filteredAlleles = Collections2.filter(snpAlleles, new Predicate<SNPAllele>() {
            @Override
            public boolean apply(@Nullable SNPAllele input) {
                if (countryFilter.getSelectedIndex() == 0) {
                    return true;
                }
                return countryFilter.getSelectedValue().equals(input.getPassport().getCollection().getLocality().getCountry());
            }
        });
        this.explorerData = DataTableUtils.createSNPAllelePhenotypeTable(filteredAlleles);
        this.allele2SnpAllele = DataTableUtils.getAlleleToPhenotype(filteredAlleles);
        this.boxPlotData = DataTableUtils.createSNPAllelePhenotypeForBoxplotTable(allele2SnpAllele, alleleInfo);
        this.stripChartData = DataTableUtils.createSNPAllelePhenotypeForStripChartTable(filteredAlleles, alleleInfo);

        // calculate box sizing
        String refAllele = alleleInfo.getRef() != null ? alleleInfo.getRef() : "N/A";
        String altAllele = alleleInfo.getAlt() != null ? alleleInfo.getAlt() : "N/A";

        int refCount = allele2SnpAllele.get(refAllele).size();
        int altCount = allele2SnpAllele.get(altAllele).size();
        double sqrtAlleleRef = Math.sqrt(refCount);
        double sqrtAlleleAlt = Math.sqrt(altCount);
        resizeRefAllele = sqrtAlleleAlt > sqrtAlleleRef;
        candleStickBoxWidth = (Math.min(sqrtAlleleRef, sqrtAlleleAlt) / Math.max(sqrtAlleleRef, sqrtAlleleAlt));
        refMeanValue = calculateMean(allele2SnpAllele.get(refAllele));
        altMeanValue = calculateMean(allele2SnpAllele.get(altAllele));

        double barSize = 100 * candleStickBoxWidth;
        refAlleleLb.setInnerText(refAllele + " : #" + refCount);
        altAlleleLb.setInnerText(altAllele + " : #" + altCount);
        refAlleleBar.getStyle().clearWidth();
        altAlleleBar.getStyle().clearWidth();
        if (resizeRefAllele) {
            refAlleleBar.getStyle().setWidth(barSize, Style.Unit.PCT);
        } else {
            altAlleleBar.getStyle().setWidth(barSize, Style.Unit.PCT);
        }
    }

    @Override
    public void displaySNPInfo(SNPGWASInfoProxy response) {
        chrLb.setInnerText(response.getChr());
        posLb.setInnerText(String.valueOf(response.getPosition()));
        scoreLb.setInnerText(String.valueOf(response.getScore()));


    }

    @Override
    public void setPhenotypeRange(Range<Double> valueRange) {
        phenotypeBarHasCell.setRange(valueRange);
    }


    @Override
    public void displayAlleInfo(SNPAnnotProxy snpAnnot) {
        this.alleleInfo = snpAnnot;
        typeLb.setInnerText(alleleInfo.getAnnotation());
        geneLb.setInnerText(alleleInfo.getGene());
    }

    @Override
    public void setList(List<SNPAllele> list) {
        sortHandler.setList(list);
    }

    private GQuery getCandleStickBar(boolean isRef) {
        GQuery rect;
        if (isRef) {
            rect = $("rect[fill=\"" + COLOR_REF + "\"][stroke=\"" + COLOR_REF + "\"]", candlestickChart);
        } else {
            rect = $("rect[fill=\"" + COLOR_ALT + "\"][stroke=\"" + COLOR_ALT + "\"]", candlestickChart);
        }
        return rect;
    }


}
