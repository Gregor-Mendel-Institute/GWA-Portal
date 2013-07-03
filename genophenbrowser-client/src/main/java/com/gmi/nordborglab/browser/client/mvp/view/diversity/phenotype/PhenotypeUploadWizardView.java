package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.ValueListBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.ui.cells.BooleanIconCell;
import com.gmi.nordborglab.browser.client.util.HTML5Helper;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadValueProxy;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.ProgressEvent;
import elemental.html.Blob;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.xml.XMLHttpRequest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.google.gwt.query.client.GQuery.$;

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

    private static class ValueColumn extends Column<PhenotypeUploadValueProxy, String> {

        private final int headerIx;

        private ValueColumn(int headerIx) {
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
    HTMLPanel fileSelectPanel;

    @UiField
    Button phenotypeFileUploadCancelBtn;

    @UiField
    Button phenotypeFileUploadStartBtn;

    @UiField
    FileUpload phenotypeFileUploadBtn;

    @UiField
    Form phenotypeUploadForm;
    @UiField
    HTML phenotypeFileDropPanel;
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
    DivElement checkFileTableContainer;
    @UiField
    DivElement phenotypeFileDropText;
    @UiField
    TableElement checkFileTable;
    @UiField
    TableElement checkMetaTable;
    @UiField
    Button phenotypeFileBrowseBtn;
    @UiField
    Button createPhenotypeBtn;
    @UiField
    Button cancelPhenotypeBtn;
    @UiField
    HTMLPanel phenotypeFormPanel;


    private final PhenotypeDriver driver;
    private boolean multipleUpload = false;
    private Map<File, Boolean> filesToUpload = Maps.newLinkedHashMap();
    private static List<String> csvMimeTypes = Lists.newArrayList("text/csv", "application/csv", "application/excel", "application/vnd.ms-excel", "application/vnd.msexcel");
    private BiMap<File, Element> filesToRow = HashBiMap.create();
    private List<String> headerColumns = ImmutableList.of("accessionid", "MEAN", "MEASURE", "STD", "MODE", "COUNT", "VARIANCE", "MEDIAN");
    private File file = null;
    private Queue<File> filesInUploadQueue = Lists.newLinkedList();
    private int currentUploadCount = 0;


    private Function clickOnCancelFileFunc = new Function() {
        @Override
        public boolean f(com.google.gwt.user.client.Event e) {
            Element elem = $(e).closest("tr").get(0);
            File file = filesToRow.inverse().get(elem);
            removeFile(file);
            return true;
        }
    };

    private Function clickOnFileFunc = new Function() {
        @Override
        public boolean f(com.google.gwt.user.client.Event e) {
            Element elem = $(e).closest("tr").get(0);
            File file = filesToRow.inverse().get(elem);
            checkFileContents(file);
            return true;
        }
    };

    @Inject
    public PhenotypeUploadWizardView(final Binder binder,
                                     final CustomDataGridResources dataGridResources, final PhenotypeDriver driver) {
        this.driver = driver;
        phenotypeValuesDataGrid = new DataGrid<PhenotypeUploadValueProxy>(50, dataGridResources);
        initCellTable();
        widget = binder.createAndBindUi(this);
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel, false);
        phenotypeValuePager.setDisplay(phenotypeValuesDataGrid);
        driver.initialize(phenotypeEditor);

        phenotypeFormPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
    }

    private void initCellTable() {

        NumberFormat format = NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0);
        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, Boolean>(new BooleanIconCell()) {
            @Override
            public Boolean getValue(PhenotypeUploadValueProxy object) {
                return (!object.hasParseError() && object.isIdKnown());
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

    private <C> void addColumn(Column<PhenotypeUploadValueProxy, C> column, String header) {
        phenotypeValuesDataGrid.addColumn(column, header);
    }


    @Override
    public Widget asWidget() {
        return widget;
    }


    @UiHandler("phenotypeFileBrowseBtn")
    public void onClickPhenotypeFileBrowseBtn(ClickEvent e) {
        phenotypeFileUploadBtn.getElement().<InputElement>cast().click();
    }

    @UiHandler("phenotypeFileUploadBtn")
    public void onHandlePhenotypeFileSelect(ChangeEvent e) {

        try {
            elemental.html.InputElement input = (elemental.html.InputElement) phenotypeFileUploadBtn.getElement();
            FileList fileList = input.getFiles();
            updateSelectedPhenotypeFileTable(fileList);
        } catch (Exception ex) {
        }
    }

    private void updateSelectedPhenotypeFileTable(FileList fileList) {
        if (fileList.length() == 0)
            return;
        if (!multipleUpload && filesToUpload.size() == 1)
            return;
        fileSelectPanel.addStyleName("in");
        int fileListLength = multipleUpload ? fileList.getLength() : 1;
        for (int i = 0; i < fileListLength; i++) {
            File file = fileList.item(i);
            boolean fileExtOk = checkFileExtOk(file);
            boolean isParseOk = true;
            filesToUpload.put(file, (fileExtOk & isParseOk));
            addFileToTable(file, fileExtOk, isParseOk);
            if (fileExtOk && isValidCSVType(file.getType())) {
                checkFileContents(file);
            }
        }
        updateFileUploadControls();
    }

    private boolean checkFileExtOk(File file) {
        String fileExt = file.getType();
        return isValidCSVType(fileExt);
    }

    private boolean isValidCSVType(String type) {
        return csvMimeTypes.contains(type);
    }

    private void addFileToTable(File file, boolean isExtOk, boolean isParseOk) {
        String nameCell = "<td>" + file.getName() + "</td>";
        String sizeCell = "<td>" + String.valueOf(Math.round(file.getSize() / 1024)) + " KB</td>";
        String extCell = "<td>" + file.getType() + "</td>";
        String progressBarCell = "";
        if (isExtOk) {
            progressBarCell = "<td><div class=\"progress progress-striped active\" style=\"width:200px\"><div class=\"bar\" style=\"width: 0%;\"></div></div></td>";
            if (isValidCSVType(file.getType()))
                nameCell = "<td><a href=\"javascript:;\">" + file.getName() + "</a></td>";
        } else {
            progressBarCell = "<td><span class=\"label label-important\">Error</span> Filetype not allowed</div></td>";
        }
        String cancelBtnCell = "<td><a href=\"javascript:;\" class=\"btn btn-warning\" style=\"\" aria-hidden=\"false\"><i class=\"icon-ban-circle\"></i> Remove </a></td>";
        GQuery row = $("<tr>" + nameCell + sizeCell + extCell + progressBarCell + cancelBtnCell + "</tr>").appendTo($("#fileToUploadTable > tbody:last"));
        Element elem = row.get(0);
        $("a", elem.getChild(0)).bind(com.google.gwt.user.client.Event.ONCLICK, clickOnFileFunc);
        $("a", elem.getChild(4)).bind(com.google.gwt.user.client.Event.ONCLICK, clickOnCancelFileFunc);
        filesToRow.put(file, elem);
    }

    @UiHandler("phenotypeFileUploadCancelBtn")
    public void onClickPhenotypeFileUploadCancelBtn(ClickEvent e) {
        resetUploadForm();
    }

    private void resetUploadForm() {
        phenotypeUploadForm.reset();
        phenotypeFileUploadStartBtn.setVisible(false);
        phenotypeFileUploadCancelBtn.setVisible(false);
        phenotypeFileBrowseBtn.setVisible(true);
        fileSelectPanel.removeStyleName("in");
        phenotypeFileDropText.getStyle().setDisplay(Style.Display.BLOCK);
        checkFileTableContainer.getStyle().setDisplay(Style.Display.NONE);
        filesToUpload.clear();
        filesToRow.clear();
        filesInUploadQueue.clear();
        clearTable();
    }


    private void clearTable() {
        $("#fileToUploadTable > tbody > tr").remove();
    }

    @UiHandler("phenotypeFileDropPanel")
    public void onPhenotypeFileDrop(DropEvent e) {
        e.stopPropagation();
        ;
        e.preventDefault();

        HTML5Helper.ExtDataTransfer dataTransfer = (HTML5Helper.ExtDataTransfer) e.getDataTransfer();
        FileList fileList = dataTransfer.getFiles();
        updateSelectedPhenotypeFileTable(fileList);
    }


    @UiHandler("phenotypeFileDropPanel")
    public void onGWASFileDragOver(DragOverEvent e) {
        e.preventDefault();
        e.stopPropagation();
    }

    @UiHandler("cancelPhenotypeBtn")
    public void onClickCancelPhenotypeBtn(ClickEvent e) {
        getUiHandlers().onCancel();
    }

    @UiHandler("createPhenotypeBtn")
    public void onClickCreatePhenotypeBtn(ClickEvent e) {
        getUiHandlers().onCreate();
    }


    @UiHandler("phenotypeFileUploadStartBtn")
    public void onClickPhenotypeFileUploadStartBtn(ClickEvent e) {
        if (countFilesWithError() > 0)
            return;
        phenotypeFileBrowseBtn.setVisible(false);
        phenotypeFileUploadCancelBtn.setVisible(false);
        phenotypeFileUploadStartBtn.setVisible(false);
        filesInUploadQueue.addAll(filesToUpload.keySet());
        //getUiHandlers().onUploadStart();
        startPartialUpload();
    }

    private void startPartialUpload() {
        int remainingUploadSlots = 3 - currentUploadCount;
        if (remainingUploadSlots > filesInUploadQueue.size())
            remainingUploadSlots = filesInUploadQueue.size();
        for (int i = 0; i < remainingUploadSlots; i++) {
            final XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
            final File file = filesInUploadQueue.poll();
            xhr.getUpload().setOnerror(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    getUiHandlers().onUploadError(xhr.getResponseText());
                    updateFileUploadStatus(file, false);
                }
            });
            xhr.getUpload().setOnprogress(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    if (event instanceof ProgressEvent) {
                        ProgressEvent progressEvent = (ProgressEvent) event;
                        if (progressEvent.isLengthComputable()) {
                            double max = progressEvent.getTotal();
                            double current = progressEvent.getLoaded();
                            updateProgressBar(file, max, current);
                        }
                    }
                }
            });
            xhr.setOnerror(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    getUiHandlers().onUploadError(xhr.getResponseText());
                    updateFileUploadStatus(file, false);
                }
            });
            xhr.setOnload(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    if (xhr.getStatus() != 200) {
                        getUiHandlers().onUploadError(xhr.getResponseText());
                        updateFileUploadStatus(file, false);
                    } else {
                        getUiHandlers().onUploadFinished(xhr.getResponseText());
                        updateFileUploadStatus(file, true);
                    }
                }
            });
            HTML5Helper.ExtJsFormData formData = HTML5Helper.ExtJsFormData.newExtJsForm();
            formData.append("file", file, file.getName());
            xhr.open("POST", GWT.getHostPageBaseURL() + "provider/phenotype/upload");
            xhr.send(formData);
            currentUploadCount += 1;
        }
        if (remainingUploadSlots == 0 && filesInUploadQueue.size() == 0) {
            updateUploadStatus();
        }
    }

    private void deccCurrentUploadCount() {
        if (currentUploadCount > 1)
            currentUploadCount -= 1;
        startPartialUpload();
    }

    private void updateFileUploadStatus(File file, boolean isSuccess) {
        Element elem = filesToRow.get(file);
        GQuery query = $(elem);
        if (isSuccess) {
            query.find("td:nth-child(4)").html("<span class=\"label label-success\">FINISHED</div>");
        } else {
            filesToUpload.put(file, false);
            query.find("td:nth-child(4)").html("<span class=\"label label-important\">FAILED</div>");
        }
        query.find("td:nth-child(5)").hide();
        updateUploadStatus();
    }

    private void updateUploadStatus() {
        //getUiHandlers().onUploadEnd();
        //gwasFileUploadCloseBtn.setVisible(true);
        GQuery query = $("#checkStatusMsg");
        int totalCount = filesToUpload.size();
        int errorCount = countFilesWithError();
        if (errorCount == 0) {
            query.html("All added files (" + totalCount + ") successfully uploaded!");
            query.closest("div").removeClass("alert-error").addClass("alert-success");
        } else {
            query.html(errorCount + " out of " + totalCount + " file(s) failed to upload!");
            query.closest("div").removeClass("alert-success").addClass("alert-error");
        }
    }


    private void updateProgressBar(File file, double max, double current) {
        Element elem = filesToRow.get(file);
        GQuery query = $(elem).find("td:nth-child(4)");
        int percentage = (int) Math.round(max / current * 100);
        query.find("div > div").css("width", percentage + "%").html(percentage + "%");
    }

    @Override
    public void showPhenotypeValuePanel(PhenotypeUploadDataProxy data) {
        resetUploadForm();
        String message = "All phentoype values successfully parsed. Click \"Create\" to finish!";
        AlertType messageType = AlertType.SUCCESS;
        if (data.getErrorValueCount() > 0) {
            messageType = AlertType.ERROR;
            message = data.getErrorValueCount() + " of " + data.getPhenotypeUploadValues().size() + " phenotype values haven an error.";
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
        resetUploadForm();
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
    public void showConstraintViolations() {

    }

    @Override
    public PhenotypeDriver getDriver() {
        return driver;
    }

    @Override
    public void showActionButtns(boolean show) {
        createPhenotypeBtn.setVisible(show);
        cancelPhenotypeBtn.setVisible(show);
    }


    public final native void logPhenotypeValue(String value) /*-{
        return $wnd.console.log(value);
    }-*/;

    private void updateFileInTable(File file) {
        if (!filesToUpload.get(file)) {
            Element elem = filesToRow.get(file);
            GQuery query = $(elem);
            query.find("td:nth-child(4)").html("<span class=\"label label-important\">Error</span> Parse error</div>");
        }
    }

    private void removeFile(final File file) {
        Element elem = filesToRow.get(file);
        $(elem).remove();
        filesToRow.remove(file);
        filesToUpload.remove(file);
        updateFileUploadControls();
        if (filesToUpload.size() == 0) {
            resetUploadForm();
        }
    }

    private boolean parseAndDisplayFileContents(String fileContent) {
        //TODO regular expression
        String[] lines = fileContent.split("\n");
        if (lines.length == 1)
            lines = fileContent.split("\r");
        String meta = null;
        String header = null;
        String firstLine = null;
        if (lines.length > 0) {
            meta = lines[0];
            if (!meta.substring(0, 7).equalsIgnoreCase("#HEADER")) {
                header = meta;
                meta = null;
                if (lines.length > 1) {
                    firstLine = lines[1];
                }
            } else {
                if (lines.length > 1) {
                    header = lines[1];
                }
                if (lines.length > 2) {
                    firstLine = lines[2];
                }
            }
        }
        boolean isMetaOK = checkMeta(meta);
        boolean isHeaderOk = checkHeader(header);
        boolean isFirstLineOk = checkFirstLine(firstLine);
        checkFileTableContainer.getStyle().setDisplay(Style.Display.BLOCK);
        phenotypeFileDropText.getStyle().setDisplay(Style.Display.NONE);
        return (isHeaderOk & isFirstLineOk & isMetaOK);
    }

    private boolean checkMeta(String meta) {
        if (meta == null) {
            checkMetaTable.getStyle().setDisplay(Style.Display.NONE);
        } else {
            checkMetaTable.getStyle().setDisplay(Style.Display.BLOCK);
            $(checkMetaTable).find("thead th").html(meta);
        }
        return true;
    }

    private boolean checkHeader(String header) {
        String[] columns = new String[0];
        if (header != null)
            columns = header.split(",");
        $(checkFileTable).find("thead tr th").remove();
        GQuery query = $(checkFileTable).find("thead tr");
        boolean isOk = true;
        if (columns.length == 0) {
            query.append("<th>accessionid [MISSING]</th>").css("color", "red");
            query.append("<th>VALUE [MISSING]</th>").css("color", "red");
            return false;
        } else if (columns.length == 1) {
            query.append("<th>" + columns[0] + "</th>").css("color", "grey");
            query.append("<th>VALUE [MISSING]</th>").css("color", "red");
        } else {
            for (int i = 0; i < columns.length; i++) {
                if (i == 0) {
                    if (columns[i].trim().equalsIgnoreCase("accessionid")) {
                        query.append("<th style=\"color:green\">accessionid</th>");
                    } else {
                        query.append("<th style=\"color:red\">accessionid [" + columns[i] + "]</th>");
                        isOk = false;
                    }
                } else {
                    String color = "green";
                    if (!headerColumns.contains(columns[i].trim())) {
                        color = "red";
                        isOk = false;
                    } else {
                    }
                    query.append("<th style=\"color:" + color + "\">" + columns[i] + "</th>");
                }
            }
        }
        return isOk;
    }


    private boolean checkFirstLine(String firstLine) {
        boolean isOk = true;
        String[] values = new String[0];
        if (firstLine != null)
            values = firstLine.split(",");

        $(checkFileTable).find("tbody tr td").remove();
        GQuery query = $(checkFileTable).find("tbody tr");
        if (values.length == 0) {
            query.append("<td>6909 [MISSING]</td>").css("color", "red");
            query.append("<th>12.5 [MISSING]</th>").css("color", "red");
            return false;
        } else if (values.length == 1) {
            query.append("<td>" + values[0] + "</td>").css("color", "grey");
            query.append("<th>12.5 [MISSING]</th>").css("color", "red");
        } else {
            for (int i = 0; i < values.length; i++) {
                String color = "green";
                if (i == 0) {
                    if (!checkLong(values[i].trim())) {
                        color = "red";
                        isOk = false;
                    }
                    query.append("<td>" + values[i] + "</td>").css("color", color);
                } else {
                    if (!checkDouble(values[i].trim())) {
                        color = "red";
                        isOk = false;
                    }
                    query.append("<td>" + values[i] + "</td>").css("color", color);
                }
            }
        }
        return isOk;
    }

    private boolean checkLong(String value) {
        boolean isOk = true;
        try {
            Long.parseLong(value);
        } catch (Exception e) {
            isOk = false;
        }
        return isOk;
    }

    private boolean checkDouble(String value) {
        boolean isOk = true;
        try {
            Double.parseDouble(value);
        } catch (Exception e) {
            isOk = false;
        }
        return isOk;
    }


    private void checkFileContents(final File file) {
        if (file == null || !isValidCSVType(file.getType()))
            return;
        FileReader reader = Browser.getWindow().newFileReader();
        Blob blob = ((HTML5Helper.ExtJsFile) file).webkitSlice(0, 100, file.getType(), "test");
        reader.addEventListener("loadend", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                FileReader reader = (FileReader) event.getTarget();
                if (reader.getReadyState() == FileReader.DONE) {
                    String fileContent = reader.getResult().toString();
                    boolean isParseOk = parseAndDisplayFileContents(fileContent);
                    $("#checkFileTableHeader").html(file.getName() + ":");
                    filesToUpload.put(file, isParseOk);
                    if (!isParseOk) {
                        updateFileInTable(file);
                        updateFileUploadControls();
                    }
                }
            }
        }, false);
        reader.readAsText(blob);
    }


    private void updateFileUploadControls() {
        boolean hasFiles = filesToUpload.size() > 0;
        phenotypeFileUploadCancelBtn.setVisible(hasFiles);
        phenotypeFileUploadStartBtn.setVisible(hasFiles);
        if (!multipleUpload && hasFiles) {
            phenotypeFileBrowseBtn.setVisible(false);
        }
        GQuery query = $("#checkStatusMsg");
        int totalCount = filesToUpload.size();
        int errorCount = countFilesWithError();
        if (errorCount == 0) {
            query.html("All added files (" + totalCount + ") are valid!");
            query.closest("div").removeClass("alert-error").addClass("alert-success");
            phenotypeFileUploadStartBtn.setEnabled(true);
        } else {
            query.html(errorCount + " out of " + totalCount + " file(s) have errors. Please fix!");
            query.closest("div").removeClass("alert-success").addClass("alert-error");
            phenotypeFileUploadStartBtn.setEnabled(false);
        }
    }

    private int countFilesWithError() {
        return Collections2.filter(filesToUpload.values(), new Predicate<Boolean>() {

            @Override
            public boolean apply(@Nullable Boolean input) {
                return !input;
            }
        }).size();
    }
}