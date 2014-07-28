package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.gmi.nordborglab.browser.client.csv.DefaultFileChecker;
import com.gmi.nordborglab.browser.client.csv.SupressException;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.events.FileUploadCloseEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadErrorEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadFinishedEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadStartEvent;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.widgets.FileUploadWidget;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.BooleanIconCell;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadValueProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtsupercsv.cellprocessor.ParseDouble;
import org.gwtsupercsv.cellprocessor.ParseInt;
import org.gwtsupercsv.cellprocessor.ParseLong;
import org.gwtsupercsv.cellprocessor.ift.CellProcessor;
import org.gwtsupercsv.io.CsvListReader;
import org.gwtsupercsv.io.ICsvListReader;
import org.gwtsupercsv.prefs.CsvPreference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:38 PM                                                                         B
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardView extends ViewWithUiHandlers<PhenotypeUploadWizardUiHandlers> implements PhenotypeUploadWizardPresenterWidget.MyView {


    interface Binder extends UiBinder<Widget, PhenotypeUploadWizardView> {

    }


    private class PhenotypeFileChecker implements FileUploadWidget.FileChecker {

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
                Set<String> headers = processors.keySet();
                CellProcessor[] cellProcessors = new CellProcessor[header.size()];
                cellProcessors[0] = new SupressException(new ParseInt());
                cellProcessors[1] = new SupressException(new ParseDouble());
                if (header.size() == 0) {
                    headerParseResults.add(new FileUploadWidget.ParseResult("MISSING", true, "accessionid"));
                    headerParseResults.add(new FileUploadWidget.ParseResult("MISSING", true, "VALUE"));
                    parseError = true;
                } else if (header.size() == 1) {
                    headerParseResults.add(new FileUploadWidget.ParseResult(header.get(0), "accessionid" != header.get(0), "accessionid"));
                    headerParseResults.add(new FileUploadWidget.ParseResult("MISSING", true, "VALUE"));
                    parseError = true;
                } else {
                    for (int i = 0; i < header.size(); i++) {
                        if (i == 0) {
                            headerParseResults.add(new FileUploadWidget.ParseResult(header.get(0), "accessionid" != header.get(0), "accessionid"));
                        } else {

                            if (headers.contains(header.get(i))) {
                                cellProcessors[i] = new SupressException(processors.get(header.get(i)));
                                headerParseResults.add(new FileUploadWidget.ParseResult(header.get(i), false, header.get(i)));
                            } else {
                                cellProcessors[i] = new SupressException(new ParseDouble());
                                headerParseResults.add(new FileUploadWidget.ParseResult("UNKOWN", true, header.get(i)));
                                parseError = true;
                            }
                        }
                    }
                }

                List<Object> firstLine = reader.read(cellProcessors);
                if (firstLine == null) {
                    parseError = true;
                }
                for (CellProcessor processor : cellProcessors) {
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

    public static class ValueColumn extends Column<PhenotypeUploadValueProxy, String> {

        private final int headerIx;

        public ValueColumn(int headerIx) {
            super(new TextCell());
            this.headerIx = headerIx;
        }

        @Override
        public String getValue(PhenotypeUploadValueProxy object) {
            return object == null ? null : object.getValues().get(headerIx);
        }
    }

    public interface PhenotypeDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeEditEditor> {
    }

    private final Widget widget;


    @UiField
    LayoutPanel uploadPhenotypePanel;
    @UiField
    LayoutPanel phenotypeValuePanel;
    @UiField
    ScrollPanel phenotypeUploadPanel;
    @UiField
    CustomPager phenotypeValuePager;
    @UiField(provided = true)
    DataGrid<PhenotypeUploadValueProxy> phenotypeValuesDataGrid;

    @UiField
    Alert phenotypeValueStatus;
    @UiField
    PhenotypeEditEditor phenotypeEditor;
    @UiField
    Button createPhenotypeBtn;
    @UiField
    Button cancelPhenotypeBtn;
    @UiField
    HTMLPanel phenotypeFormPanel;
    @UiField
    FileUploadWidget fileUploadWidget;


    private final PhenotypeDriver driver;
    private final String restURL = "/provider/phenotype/upload";
    private static List<String> csvMimeTypes = Lists.newArrayList("text/csv", "application/csv", "application/excel", "application/vnd.ms-excel", "application/vnd.msexcel", "text/comma-separated-values");
    private final List<String> allowedExtensions = Lists.newArrayList();
    private List<String> headerColumns = ImmutableList.of("accessionid", "MEAN", "MEASURE", "STD", "MODE", "COUNT", "VARIANCE", "MEDIAN");
    private List<String> defaultValues = ImmutableList.of("6909", "12.2", "20.2", "12.23", "30", "40", "12.5", "14.5");
    private FileUploadWidget.FileChecker fileChecker;
    private final Map<String, CellProcessor> processors;


    @Inject
    public PhenotypeUploadWizardView(final Binder binder,
                                     final CustomDataGridResources dataGridResources, final PhenotypeDriver driver,
                                     final OntologyManager ontologyManager) {
        this.driver = driver;
        processors = ImmutableMap.<String, CellProcessor>builder()
                .put("accessionid", new ParseLong())
                .put("MEAN", new ParseDouble())
                .put("MEASURE", new ParseDouble())
                .put("STD", new ParseDouble())
                .put("MODE", new ParseInt())
                .put("COUNT", new ParseInt())
                .put("VARIANCE", new ParseDouble())
                .put("MEDIAN", new ParseDouble()).build();
        phenotypeValuesDataGrid = new DataGrid<PhenotypeUploadValueProxy>(50, dataGridResources);
        initCellTable();
        widget = binder.createAndBindUi(this);
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel, false);
        phenotypeValuePager.setDisplay(phenotypeValuesDataGrid);
        driver.initialize(phenotypeEditor);
        phenotypeEditor.setOntologyManager(ontologyManager);
        phenotypeFormPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        initFileUploadWidget();
    }

    private void initFileUploadWidget() {

        fileChecker = new PhenotypeFileChecker();
        fileUploadWidget.setMultiUpload(false);
        fileUploadWidget.setAdditionalInfo("At the minimum the <b>accessionid</b> column and one <b>value column</b> (i.e. MEAN) have to be provided");
        fileUploadWidget.setRestURL(restURL);
        fileUploadWidget.setFileChecker(fileChecker);
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

    private void initCellTable() {

        NumberFormat format = NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0);
        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, Boolean>(new BooleanIconCell()) {
            @Override
            public Boolean getValue(PhenotypeUploadValueProxy object) {
                return (!object.isParseError() && object.isIdKnown());
            }
        });


        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, Number>(new NumberCell(format)) {
            @Override
            public Long getValue(PhenotypeUploadValueProxy object) {
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
        }, "ID");

        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, String>(new TextCell()) {
            @Override
            public String getValue(PhenotypeUploadValueProxy object) {
                return object == null ? null : object.getAccessionName();
            }
        }, "Name");

        phenotypeValuesDataGrid.setColumnWidth(0, "50px");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }



    @UiHandler("cancelPhenotypeBtn")
    public void onClickCancelPhenotypeBtn(ClickEvent e) {
        getUiHandlers().onCancel();
    }

    @UiHandler("createPhenotypeBtn")
    public void onClickCreatePhenotypeBtn(ClickEvent e) {
        getUiHandlers().onCreate();
    }


    @Override
    public void showPhenotypeValuePanel(PhenotypeUploadDataProxy data) {
        fileUploadWidget.resetUploadForm();
        String message = "All phentoype values successfully parsed. Click \"Create\" to finish!";
        AlertType messageType = AlertType.SUCCESS;
        if (data.getErrorValueCount() > 0) {
            messageType = AlertType.ERROR;
            message = data.getErrorValueCount() + " of " + data.getPhenotypeUploadValues().size() + " phenotype values haven an error. They will be removed!";
        }
        if (data.getErrorMessage() != null && !data.getErrorMessage().equals("")) {
            messageType = AlertType.ERROR;
            message = data.getErrorMessage();
        }
        phenotypeValueStatus.setType(messageType);
        phenotypeValueStatus.setText(message);
        phenotypeValueStatus.setVisible(true);

        uploadPhenotypePanel.setWidgetVisible(phenotypeUploadPanel, false);
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel, true);
    }

    @Override
    public void showPhenotypeUploadPanel() {
        fileUploadWidget.resetUploadForm();
        phenotypeValueStatus.setVisible(false);
        phenotypeValueStatus.setText("");
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel, false);
        uploadPhenotypePanel.setWidgetVisible(phenotypeUploadPanel, true);
    }


    @Override
    public void setUnitOfMeasureList(List<UnitOfMeasureProxy> unitOfMeasureList) {
        phenotypeEditor.setAcceptableValuesForUnitOfMeasure(unitOfMeasureList);
    }

    @Override
    public HasData<PhenotypeUploadValueProxy> getPhenotypeValueDisplay() {
        return phenotypeValuesDataGrid;
    }

    @Override
    public void addColumns(List<String> valueColumns) {
        for (int i = 3; i < phenotypeValuesDataGrid.getColumnCount(); i++) {
            phenotypeValuesDataGrid.removeColumn(i);
        }

        for (int i = 0; i < valueColumns.size(); i++) {
            phenotypeValuesDataGrid.addColumn(new ValueColumn(i), valueColumns.get(i));
        }
    }


    @Override
    public PhenotypeDriver getDriver() {
        return driver;
    }
}