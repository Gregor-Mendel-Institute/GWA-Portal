package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload;

import com.gmi.nordborglab.browser.client.csv.DefaultFileChecker;
import com.gmi.nordborglab.browser.client.csv.SupressException;
import com.gmi.nordborglab.browser.client.editors.ExperimentEditEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeUploadDataListEditor;
import com.gmi.nordborglab.browser.client.events.FileUploadCloseEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadErrorEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadFinishedEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadStartEvent;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.resources.CardCellListResources;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.client.ui.card.PhenotypeUploadDataCardCell;
import com.gmi.nordborglab.browser.client.ui.cells.BooleanIconCell;
import com.gmi.nordborglab.browser.client.ui.cells.FlagCell;
import com.gmi.nordborglab.browser.client.ui.fileupload.FileUploadWidget;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampleDataProxy;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Booleans;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.MouseEvent;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.googlecode.gwt.charts.client.corechart.Histogram;
import com.googlecode.gwt.charts.client.geochart.GeoChart;
import com.googlecode.gwt.charts.client.geochart.GeoChartOptions;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtsupercsv.cellprocessor.Optional;
import org.gwtsupercsv.cellprocessor.ParseDouble;
import org.gwtsupercsv.cellprocessor.ParseInt;
import org.gwtsupercsv.cellprocessor.constraint.NotNull;
import org.gwtsupercsv.cellprocessor.ift.CellProcessor;
import org.gwtsupercsv.io.CsvListReader;
import org.gwtsupercsv.io.ICsvListReader;
import org.gwtsupercsv.prefs.CsvPreference;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:38 PM                                                                         B
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardView extends ViewWithUiHandlers<PhenotypeUploadWizardUiHandlers> implements PhenotypeUploadWizardPresenterWidget.MyView, Editor<ExperimentUploadDataProxy> {


    interface Binder extends UiBinder<Widget, PhenotypeUploadWizardView> {
    }

    public interface ExperimentUploadDataEditDriver extends RequestFactoryEditorDriver<ExperimentUploadDataProxy, PhenotypeUploadWizardView> {
    }

    public interface MyStyle extends CssResource {
        String errorHeader();
    }

    private static class PhenotypeFileChecker implements FileUploadWidget.FileChecker {

        private final List<String> allowedExtensions;
        private final List<String> csvMimeTypes;
        private final List<String> headerColumns;
        private final List<String> defaultValues;

        public PhenotypeFileChecker(List<String> allowedExtensions, List<String> csvMimeTypes, List<String> headerColumns, List<String> defaultValues) {
            this.allowedExtensions = allowedExtensions;
            this.csvMimeTypes = csvMimeTypes;
            this.headerColumns = headerColumns;
            this.defaultValues = defaultValues;
        }

        @Override
        public boolean isValidExtension(String extension) {
            return allowedExtensions.contains(extension) || csvMimeTypes.contains(extension);
        }

        @Override
        public boolean canParse(String extension) {
            return true;
        }

        @Override
        public boolean parse(String content, FileUploadWidget.FileCheckerResult result) {
            boolean parseError = false;
            List<FileUploadWidget.ParseResult> headerParseResults = Lists.newArrayList();
            List<FileUploadWidget.ParseResult> firstLineParseResults = Lists.newArrayList();
            ICsvListReader reader = null;
            try {
                reader = new CsvListReader(content, CsvPreference.STANDARD_PREFERENCE);
                List<String> header = reader.read();
                CellProcessor[] cellValueProcessors = new CellProcessor[header.size()];
                CellProcessor[] cellHeaderProcessors = new CellProcessor[header.size()];
                cellValueProcessors[0] = new SupressException(new ParseInt());
                cellHeaderProcessors[0] = new SupressException(new NotNull());
                for (int i = 1; i < cellValueProcessors.length; i++) {
                    cellValueProcessors[i] = new SupressException(new Optional(new ParseDouble()));
                    cellHeaderProcessors[i] = new SupressException(new NotNull());
                }
                if (header.size() == 0) {
                    headerParseResults.add(new FileUploadWidget.ParseResult("MISSING", true, "accessionid"));
                    headerParseResults.add(new FileUploadWidget.ParseResult("MISSING", true, "Phenotype1"));
                    parseError = true;
                } else if (header.size() == 1) {
                    headerParseResults.add(new FileUploadWidget.ParseResult(header.get(0), false, header.get(0)));
                    headerParseResults.add(new FileUploadWidget.ParseResult("MISSING", true, "Phenotype1"));
                    parseError = true;
                } else {
                    reader.executeProcessors(cellHeaderProcessors);
                    for (int i = 0; i < header.size(); i++) {
                        SupressException cell = (SupressException) cellHeaderProcessors[i];
                        if (cell.getSuppressedException() != null) {
                            parseError = true;
                        }
                        headerParseResults.add(DefaultFileChecker.getParseResultFromHeader(cell));
                    }
                }

                List<Object> firstLine = reader.read(cellValueProcessors);
                if (firstLine == null) {
                    parseError = true;
                }
                for (CellProcessor processor : cellValueProcessors) {
                    SupressException cell = (SupressException) processor;
                    if (cell.getSuppressedException() != null) {
                        parseError = true;
                    }
                    firstLineParseResults.add(DefaultFileChecker.getParseResultFromFirstLine(cell));
                }
            } catch (Exception e) {
                parseError = true;
                result.setParseErrorMsg(e.getMessage());
            } finally {

            }
            result.setParsedFirstLineResult(firstLineParseResults);
            result.setParsedHeaderResult(headerParseResults);
            result.setHasParseErrors(parseError);
            return parseError;
        }

        @Override
        public String getSupportedFileTypes() {
            return Joiner.on(", ").join(Iterables.concat(csvMimeTypes, allowedExtensions));
        }

        @Override
        public List<String> getCSVHeaderFormat() {
            return headerColumns;
        }

        @Override
        public List<String> getCSVContentFormat() {
            return defaultValues;
        }
    }


    private class ValueColumn extends Column<SampleDataProxy, String> {

        private int headerIx;

        public ValueColumn(int headerIx) {
            super(new TextCell());
            this.headerIx = headerIx;
        }

        @Override
        public String getValue(SampleDataProxy object) {
            return object == null ? null : object.getValues().get(headerIx);
        }

        public void setHeaderIx(int headerIx) {
            this.headerIx = headerIx;
        }

        @Override
        public String getCellStyleNames(Cell.Context context, SampleDataProxy object) {
            if ((object.getParseMask() & (1 << (headerIx + 1))) > 0) {
                return style.errorHeader();
            }
            return super.getCellStyleNames(context, object);
        }
    }


    public class ErrorHeader extends Header<String> {

        protected final PhenotypeUploadDataProxy phenotype;

        public ErrorHeader(PhenotypeUploadDataProxy phenotype) {
            super(new TextCell());
            this.phenotype = phenotype;
        }

        @Override
        public String getHeaderStyleNames() {
            if (phenotype.getErrorCount() > 0) {
                return style.errorHeader();
            }
            return null;
        }

        @Override
        public String getValue() {
            return phenotype.getTraitUom().getLocalTraitName();
        }
    }

    private static class BooleanColumn extends Column<SampleDataProxy, Boolean> {

        private Integer phenotypeIdx;

        public BooleanColumn(Integer phenotypeIdx) {
            super(new BooleanIconCell());
            this.phenotypeIdx = phenotypeIdx;
        }

        @Override
        public Boolean getValue(SampleDataProxy object) {
            if (phenotypeIdx == null) {
                return (!object.isParseError() && object.isIdKnown() && object.getParseMask() == 0);
            } else {
                return (!object.isParseError() && object.isIdKnown() && (object.getParseMask() & (1 << phenotypeIdx + 1)) == 0);
            }
        }

        public void setPhenotypeIdx(int phenotypeIdx) {
            this.phenotypeIdx = phenotypeIdx;
        }

        public Integer getPhenotypeIdx() {
            return phenotypeIdx;
        }

        public void setPhenotypeIdx(Integer phenotypeIdx) {
            this.phenotypeIdx = phenotypeIdx;
        }
    }


    public interface PhenotypeDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeEditEditor> {
    }

    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };


    private boolean layoutScheduled = false;

    @UiField(provided = true)
    CellList<PhenotypeUploadDataProxy> phenotypeList;


    @UiField(provided = true)
    MainResources mainRes;

    @UiField
    DeckLayoutPanel contentContainer;
    @UiField
    FileUploadWidget fileUploadWidget;
    @UiField
    LayoutPanel detailStudyPanel;
    @UiField
    DockLayoutPanel resultPanel;
    @UiField
    LayoutPanel detailPhenotypePanel;

    @UiField(provided = true)
    @Editor.Path("phenotypes")
    PhenotypeUploadDataListEditor phenotypeListEditor;


    @UiField
    SpanElement overviewStatus;

    @UiField
    ScrollPanel phenotypeUploadPanel;

    DataGrid<SampleDataProxy> phenotypeValuesDataGrid;
    @UiField(provided = true)
    DataGrid<SampleDataProxy> summaryDataGrid;
    @UiField
    CustomPager summaryDataGridPager;

    @UiField
    MyStyle style;
    @UiField
    SpanElement saveMessage;
    @UiField
    Button saveBtn;
    @UiField
    com.google.gwt.user.client.ui.Button tableChartBtn;
    @UiField
    HTMLPanel histogramChartBtnContainer;
    @UiField
    HTMLPanel motionChartBtnContainer;
    @UiField
    HTMLPanel geoChartBtnContainer;
    @UiField
    com.google.gwt.user.client.ui.Button geoChartBtn;
    @UiField
    com.google.gwt.user.client.ui.Button motionChartBtn;
    @UiField
    com.google.gwt.user.client.ui.Button histogramChartBtn;
    @UiField
    HTMLPanel tableChartBtnContainer;
    @UiField
    SimpleLayoutPanel chartContainer;
    @UiField
    LayoutPanel vizContainer;
    @UiField
    LayoutPanel phenotypeDetailContainer;
    @UiField
    HTMLPanel mapBtnContainer;
    @UiField
    @Path("experiment")
    ExperimentEditEditor experimentEditor;
    @UiField
    LayoutPanel summaryContainer;
    @UiField
    HTMLPanel experimentEditorContainer;
    @UiField
    LayoutPanel summaryTableContainer;
    @UiField
    SimplePager phenotypeListPager;


    private final ChangeHandler changeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            getUiHandlers().updatePhenotypeData();
        }
    };
    private final CardCellListResources cardCellListResources;
    private final PhenotypeUploadDataCardCell phenotypeUploadDataCard;
    private static final String restURL = "/provider/phenotype/upload";
    private static final String restISATABURL = "/provider/isatab/upload";
    private static final List<String> csvMimeTypes = Lists.newArrayList("text/txt", "text/csv", "application/csv", "application/excel", "application/vnd.ms-excel", "application/vnd.msexcel", "text/comma-separated-values");
    private final List<String> allowedISAExtensions = Lists.newArrayList("application/zip", "application/x-gzip");
    private List<String> headerColumns = ImmutableList.of("accessionid", "Phenotype1", "Phenotype2", "Phenotype3", "PhenotypeN");
    private List<String> defaultValues = ImmutableList.of("6909", "12.2", "-", "12.23", "12.5");
    private FileUploadWidget.FileChecker fileChecker;
    private final ExperimentUploadDataEditDriver experimentUploadDataEditDriver;
    private final ValueColumn valueColumn = new ValueColumn(0);
    private ColumnSortEvent.ListHandler<SampleDataProxy> sortHandler = new ColumnSortEvent.ListHandler<>(null);
    private ResizeableMotionChart motionChart;
    private Histogram histogramChart;
    private GeoChart geoChart;
    private final FlagMap flagMap;

    private ImmutableSet<Marker> markers;
    private MapWidget mapWidget;
    private InfoWindow iw = InfoWindow.newInstance(null);

    private DataTable explorerData;
    private com.googlecode.gwt.charts.client.DataTable geoChartData;
    private com.googlecode.gwt.charts.client.DataTable histogramData;
    private int index;

    private static enum CHART_TYPE {
        TABLE, HISTOGRAM, GEOCHART, MAP, EXPLORER
    }

    private final List<HandlerRegistration> handlerRegistrations = Lists.newArrayList();

    private CHART_TYPE activeChartType = CHART_TYPE.TABLE;

    @Inject
    public PhenotypeUploadWizardView(final Binder binder,
                                     final CardCellListResources cardCellListResources,
                                     final CustomDataGridResources dataGridResources,
                                     final OntologyManager ontologyManager,
                                     final PhenotypeUploadDataCardCell phenotypeUploadDataCard,
                                     final CurrentUser currentUser,
                                     final ExperimentUploadDataEditDriver experimentUploadDataEditDriver,
                                     final MainResources mainRes,
                                     final FlagMap flagMap) {
        this.mainRes = mainRes;
        this.flagMap = flagMap;
        this.cardCellListResources = cardCellListResources;
        this.phenotypeUploadDataCard = phenotypeUploadDataCard;
        this.experimentUploadDataEditDriver = experimentUploadDataEditDriver;
        phenotypeValuesDataGrid = new DataGrid<>(Integer.MAX_VALUE, dataGridResources);
        summaryDataGrid = new DataGrid<>(50, dataGridResources);
        initDataGrid();
        this.phenotypeListEditor = new PhenotypeUploadDataListEditor(ontologyManager, currentUser.getAppData().getUnitOfMeasureList(), changeHandler);
        phenotypeList = new CellList<>(phenotypeUploadDataCard, cardCellListResources, PhenotypeUploadWizardPresenterWidget.phenotypeUploadKey);
        initWidget(binder.createAndBindUi(this));
        phenotypeListPager.setPageSize(25);
        phenotypeListPager.setDisplay(phenotypeList);
        vizContainer.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        vizContainer.getElement().getFirstChildElement().getNextSiblingElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        this.experimentUploadDataEditDriver.initialize(this);
        contentContainer.showWidget(0);
        ((DeckLayoutPanel) asWidget()).showWidget(0);
        //phenotypeValuePager.setDisplay(phenotypeValuesDataGrid);
        summaryDataGridPager.setDisplay(summaryDataGrid);
        initFileUploadWidget(false);
        initFileUploadHandlers();
        geoChart = new GeoChart();
        histogramChart = new Histogram();
        initMap();
    }

    private void initFileUploadHandlers() {
        fileUploadWidget.addHandler(new FileUploadCloseEvent.FileUploadCloseHandler() {
            @Override
            public void onFileUploadClose(FileUploadCloseEvent event) {
                getUiHandlers().onCancel();
            }
        }, FileUploadCloseEvent.TYPE);

        fileUploadWidget.addHandler(new FileUploadStartEvent.FileUploadStartHandler() {

            @Override
            public void onFileUploadStart(FileUploadStartEvent event) {
                getUiHandlers().startUpload();
            }
        }, FileUploadStartEvent.TYPE);

        fileUploadWidget.addHandler(new FileUploadFinishedEvent.FileUploadFinishedHandler() {
            @Override
            public void onFileUploadFinished(FileUploadFinishedEvent event) {
                getUiHandlers().onUploadFinished(event.getResponseText());
            }
        }, FileUploadFinishedEvent.TYPE);

        fileUploadWidget.addHandler(new FileUploadErrorEvent.FileUploadErrorHandler() {
            @Override
            public void onFileUploadError(FileUploadErrorEvent event) {
                getUiHandlers().onUploadError(event.getResponseText());
            }
        }, FileUploadErrorEvent.TYPE);
    }

    private void initFileUploadWidget(boolean isaTab) {
        boolean multiUpload = false;
        fileChecker = new PhenotypeFileChecker(Lists.<String>newArrayList(), csvMimeTypes, headerColumns, defaultValues);
        String additionalText = "At the minimum the <b>accessionid</b> column and one <b>Phenotype</b> column (i.e. FT10) have to be provided. Uploading multiple phenotypes can be done by adding additional columns (Phenotype2,...PhenotypeN)";
        String url = restURL;
        if (isaTab) {
            url = restISATABURL;
            fileChecker = new DefaultFileChecker(allowedISAExtensions, Lists.<String>newArrayList(), null, null);
            additionalText = "";
        }
        fileUploadWidget.setMultiUpload(multiUpload);
        fileUploadWidget.setAdditionalInfo(additionalText);
        fileUploadWidget.setRestURL(url);
        fileUploadWidget.setFileChecker(fileChecker);
    }

    private void initMap() {

        if (mapWidget != null)
            return;
        MapOptions opts = MapOptions.newInstance();
        opts.setZoom(3);
        opts.setMapTypeId(MapTypeId.TERRAIN);
        mapWidget = new MapWidget(opts);
        mapWidget.setSize("100%", "100%");
    }

    private List<Column<SampleDataProxy, ?>> getDataGridColumns() {
        NumberFormat format = NumberFormat.getFormat("#");
        List<Column<SampleDataProxy, ?>> columns = Lists.newArrayList();
        columns.add(new Column<SampleDataProxy, Number>(new NumberCell(format)) {
            @Override
            public Long getValue(SampleDataProxy object) {
                if (object == null)
                    return null;
                Long id = null;
                if (object.getPassportId() != null)
                    id = object.getPassportId();
                else {
                    try {
                        id = Long.parseLong(object.getSourceId());
                    } catch (Exception e) {

                    }
                }
                return id;
            }
        });
        columns.add(new Column<SampleDataProxy, String>(
                new FlagCell(flagMap)) {
            @Override
            public String getValue(SampleDataProxy object) {
                String icon = null;
                try {
                    icon = object.getCountryShort();
                } catch (NullPointerException e) {

                }
                return icon;
            }
        });
        columns.add(new Column<SampleDataProxy, String>(new TextCell()) {
            @Override
            public String getValue(SampleDataProxy object) {
                return object == null ? null : object.getAccessionName();
            }
        });
        return columns;
    }

    private void initDataGrid() {

        List<Column<SampleDataProxy, ?>> columns = getDataGridColumns();

        phenotypeValuesDataGrid.addColumn(new BooleanColumn(0));
        summaryDataGrid.addColumn(new BooleanColumn(null));

        phenotypeValuesDataGrid.addColumn(columns.get(0), "ID");
        summaryDataGrid.addColumn(columns.get(0), "ID");

        phenotypeValuesDataGrid.addColumn(columns.get(1), "Country");
        summaryDataGrid.addColumn(columns.get(1), "Country");

        phenotypeValuesDataGrid.addColumn(columns.get(2), "Name");
        summaryDataGrid.addColumn(columns.get(2), "Name");

        phenotypeValuesDataGrid.setColumnWidth(0, "30px");
        summaryDataGrid.setColumnWidth(0, "30px");

        phenotypeValuesDataGrid.setColumnWidth(1, 60, Style.Unit.PX);
        summaryDataGrid.setColumnWidth(1, 60, Style.Unit.PX);

        phenotypeValuesDataGrid.setColumnWidth(2, 70, Style.Unit.PX);
        summaryDataGrid.setColumnWidth(2, 70, Style.Unit.PX);


        phenotypeValuesDataGrid.addColumn(valueColumn, "Phenotype");
        summaryDataGrid.setMinimumTableWidth(600, Style.Unit.PX);

        sortHandler.setComparator(summaryDataGrid.getColumn(0), new Comparator<SampleDataProxy>() {
            @Override
            public int compare(SampleDataProxy o1, SampleDataProxy o2) {
                return Booleans.compare(o1.isParseError() || !o1.isIdKnown() || o1.getParseMask() > 0, o2.isParseError() || !o2.isIdKnown() || o2.getParseMask() > 0);
            }
        });

        summaryDataGrid.addColumnSortHandler(sortHandler);
        summaryDataGrid.getColumnSortList().push(summaryDataGrid.getColumn(0));
        summaryDataGrid.getColumn(0).setDefaultSortAscending(true);
        phenotypeValuesDataGrid.getColumnSortList().push(phenotypeValuesDataGrid.getColumn(0));
        phenotypeValuesDataGrid.getColumn(0).setDefaultSortAscending(true);


    }

    @Override
    public void updateTableWidth(int numberOfPhenotypes) {
        int minimumWidth = 160 + 80 * numberOfPhenotypes;
        summaryDataGrid.setMinimumTableWidth(minimumWidth, Style.Unit.PX);
    }



    @Override
    public void setErrorCount(int totalCount, int count) {
        fileUploadWidget.resetUploadForm();
        if (count == 0) {
            StringBuilder textB = new StringBuilder();
            if (totalCount == 1) {
                textB.append("Phenotypes is valid.");
            } else {
                textB.append("All " + totalCount + " phenotypes are valid.");

            }
            textB.append(" Press \"Save\" to finish");
            overviewStatus.setInnerText(textB.toString());
            overviewStatus.removeClassName("alert-warning");
            overviewStatus.addClassName("alert-success");
            saveBtn.setEnabled(true);
            saveMessage.getStyle().setDisplay(Style.Display.NONE);

        } else {
            overviewStatus.setInnerText(count + " phenotype" + (count > 1 ? "s have" : " has") + " an error. Select a card from the left side for details");
            overviewStatus.removeClassName("alert-success");
            overviewStatus.addClassName("alert-warning");
            saveBtn.setEnabled(false);
            saveMessage.getStyle().setDisplay(Style.Display.INLINE);
        }
    }


    @Override
    public void showPhenotypeDetailPanel(int index) {
        if (index < 0)
            return;
        this.index = index;
        valueColumn.setHeaderIx(index);
        ((BooleanColumn) phenotypeValuesDataGrid.getColumn(0)).setPhenotypeIdx(index);
        contentContainer.showWidget(1);
        phenotypeListEditor.showSubEditor(index);
        // update phenotypeUploadValueStatus
    }

    @Override
    public HasData<PhenotypeUploadDataProxy> getPhenotypeUploadList() {
        return phenotypeList;
    }

    @Override
    public void showFileUploadPanel() {
        ((DeckLayoutPanel) asWidget()).showWidget(0);
    }

    @Override
    public void resetFileUploadPanel() {
        fileUploadWidget.resetUploadForm();
    }


    private void initMarkers(Collection<SampleDataProxy> data) {
        clearMarkers();
        Set<Long> passportSet = Sets.newHashSet();
        ImmutableSet.Builder<Marker> markers = ImmutableSet.builder();
        for (SampleDataProxy sample : data) {
            if (!sample.isIdKnown() || passportSet.contains(sample.getPassportId()) ||
                    (sample.getLatitude() == null || sample.getLongitude() == null))
                continue;
            MarkerOptions options = MarkerOptions.newInstance();
            final Marker marker = Marker.newInstance(options);
            LatLng position = LatLng.newInstance(sample.getLatitude(), sample.getLongitude());
            marker.setPosition(position);
            marker.setTitle(sample.getAccessionName() + " (" + sample.getPassportId() + ")");
            marker.setMap(mapWidget);
            handlerRegistrations.add(marker.addClickHandler(new ClickMapHandler() {
                @Override
                public void onEvent(ClickMapEvent event) {
                    drawInfoWindow(marker, event.getMouseEvent());
                }
            }));
            markers.add(marker);

        }

    }

    private void drawInfoWindow(Marker marker, MouseEvent mouseEvent) {
        if (marker == null || mouseEvent == null) {
            return;
        }

        HTML html = new HTML(marker.getTitle());

        InfoWindowOptions options = InfoWindowOptions.newInstance();
        iw.setContent(html);
        iw.open(mapWidget, marker);
    }

    private void clearMarkers() {
        if (markers == null)
            return;
        for (HandlerRegistration registration : handlerRegistrations) {
            registration.removeHandler();
        }
        handlerRegistrations.clear();
        for (Marker marker : markers) {
            marker.clear();
        }
    }



    @Override
    public void scheduledLayout() {
        if (asWidget().isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }



    private void forceLayout() {
        if (!asWidget().isAttached() || !asWidget().isVisible())
            return;
        showPanel();
    }

    @Override
    public void showOverViewPanel() {
        ((DeckLayoutPanel) asWidget()).showWidget(1);
        contentContainer.showWidget(0);
    }

    @UiHandler("motionChartBtn")
    public void onMotionChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.EXPLORER);
    }

    @UiHandler("tableChartBtn")
    public void onTableChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.TABLE);
    }

    @UiHandler("histogramChartBtn")
    public void onHistogramChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.HISTOGRAM);
    }

    @UiHandler("geoChartBtn")
    public void onGeoChartBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.GEOCHART);
    }

    @UiHandler("mapBtn")
    public void onMapBtn(ClickEvent e) {
        updateChartType(CHART_TYPE.MAP);
    }

    private void updateChartType(CHART_TYPE newChartType) {
        if (this.activeChartType == newChartType)
            return;
        this.activeChartType = newChartType;

        tableChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        motionChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        histogramChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());

        geoChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());

        mapBtnContainer.removeStyleName(mainRes.style().iconContainer_active());
        switch (this.activeChartType) {
            case EXPLORER:
                motionChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
            case TABLE:
                tableChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
            case HISTOGRAM:
                histogramChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
            case GEOCHART:
                geoChartBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
            case MAP:
                mapBtnContainer.addStyleName(mainRes.style().iconContainer_active());
                break;
        }

        showPanel();
    }


    @Override
    public void resetChartData() {
        explorerData = null;
        histogramData = null;
        geoChartData = null;
        clearMarkers();
    }

    private void showPanel() {
        chartContainer.clear();
        switch (activeChartType) {
            case TABLE:
                chartContainer.add(phenotypeValuesDataGrid);
                getUiHandlers().updateTable();
                break;
            case EXPLORER:
                if (explorerData == null) {
                    explorerData = DataTableUtils.createSampleDataTable(getUiHandlers().getExplorerData(), index);
                }
                motionChart = new ResizeableMotionChart(explorerData,
                        createMotionChartOptions());
                chartContainer.add(motionChart);
                break;
            case HISTOGRAM:
                chartContainer.add(histogramChart);
                if (histogramData == null) {
                    histogramData = DataTableUtils.createPhenotypeHistogramTable2(getUiHandlers().getHistogramData());
                }
                histogramChart.draw(histogramData, DataTableUtils.getDefaultPhenotypeHistogramOptions2());
                break;
            case GEOCHART:
                chartContainer.add(geoChart);
                if (geoChartData == null) {
                    geoChartData = DataTableUtils.createPhenotypeGeoChartTable2(getUiHandlers().getGeoChartdata());
                }
                geoChart.draw(geoChartData, createGeoChart());
                break;
            case MAP:
                chartContainer.add(mapWidget);
                if (markers == null) {
                    initMarkers(getUiHandlers().getExplorerData());
                }
                mapWidget.triggerResize();
                break;

        }
    }

    private GeoChartOptions createGeoChart() {
        GeoChartOptions options = GeoChartOptions.create();
        //options.setTitle("Geographic distribution");
        options.setHeight(chartContainer.getOffsetHeight());
        return options;
    }

    private MotionChart.Options createMotionChartOptions() {
        MotionChart.Options options = MotionChart.Options.create();
        options.set(
                "state",
                "%7B%22time%22%3A%22notime%22%2C%22iconType%22%3A%22BUBBLE%22%2C%22xZoomedDataMin%22%3Anull%2C%22yZoomedDataMax%22%3Anull%2C%22xZoomedIn%22%3Afalse%2C%22iconKeySettings%22%3A%5B%5D%2C%22showTrails%22%3Atrue%2C%22xAxisOption%22%3A%222%22%2C%22colorOption%22%3A%224%22%2C%22yAxisOption%22%3A%223%22%2C%22playDuration%22%3A15%2C%22xZoomedDataMax%22%3Anull%2C%22orderedByX%22%3Afalse%2C%22duration%22%3A%7B%22multiplier%22%3A1%2C%22timeUnit%22%3A%22none%22%7D%2C%22xLambda%22%3A1%2C%22orderedByY%22%3Afalse%2C%22sizeOption%22%3A%22_UNISIZE%22%2C%22yZoomedDataMin%22%3Anull%2C%22nonSelectedAlpha%22%3A0.4%2C%22stateVersion%22%3A3%2C%22dimensions%22%3A%7B%22iconDimensions%22%3A%5B%22dim0%22%5D%7D%2C%22yLambda%22%3A1%2C%22yZoomedIn%22%3Afalse%7D%3B");
        options.setHeight(chartContainer.getOffsetHeight());
        options.setWidth(chartContainer.getOffsetWidth());
        return options;
    }


    @Override
    public HasData<SampleDataProxy> getSampleDataDisplay() {
        return phenotypeValuesDataGrid;
    }

    @Override
    public HasData<SampleDataProxy> getSummaryDisplay() {
        return summaryDataGrid;
    }

    @Override
    public void addColumns(List<PhenotypeUploadDataProxy> valueColumns) {
        for (int i = summaryDataGrid.getColumnCount() - 1; i >= 3; i--) {
            summaryDataGrid.removeColumn(i);
        }

        for (int i = 0; i < valueColumns.size(); i++) {
            summaryDataGrid.addColumn(new ValueColumn(i), new ErrorHeader(valueColumns.get(i)));
        }
    }

    @Override
    public void showExperimentEditor(boolean show) {
        summaryContainer.setWidgetVisible(experimentEditorContainer, show);
        if (!show) {
            summaryContainer.setWidgetTopBottom(summaryTableContainer, 0, Style.Unit.PX, 0, Style.Unit.PX);
        } else {
            summaryContainer.setWidgetTopBottom(summaryTableContainer, 250, Style.Unit.PX, 0, Style.Unit.PX);
        }

        initFileUploadWidget(show);
    }

    @UiHandler("saveBtn")
    public void onClickSaveBtn(ClickEvent e) {
        getUiHandlers().onCreate();
    }

    @UiHandler("cancelBtn")
    public void onClickCancelBtn(ClickEvent e) {
        getUiHandlers().onCancel();
    }

    @UiHandler("backLink")
    public void onClickBackLink(ClickEvent event) {
        getUiHandlers().deselectPhenotypeCard();
    }

    @Override
    public ExperimentUploadDataEditDriver getDriver() {
        return this.experimentUploadDataEditDriver;
    }
}