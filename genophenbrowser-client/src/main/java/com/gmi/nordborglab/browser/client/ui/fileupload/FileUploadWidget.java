package com.gmi.nordborglab.browser.client.ui.fileupload;

import com.gmi.nordborglab.browser.client.events.FileUploadCloseEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadEndEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadErrorEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadFinishedEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadStartEvent;
import com.gmi.nordborglab.browser.client.util.HTML5Helper;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.ProgressEvent;
import elemental.html.Blob;
import elemental.html.File;
import elemental.html.FileList;
import elemental.html.FileReader;
import elemental.xml.XMLHttpRequest;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Form;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.google.gwt.query.client.GQuery.$;

/**
 * Created by uemit.seren on 5/28/14.
 */
public class FileUploadWidget extends Composite {


    interface Binder extends UiBinder<Container, FileUploadWidget> {
    }

    private static Binder binder = GWT.create(Binder.class);

    public static class ParseResult {

        private final String value;
        private final String expectedValue;
        private final boolean hasParseError;

        public ParseResult(String value, boolean hasParseError, String expectedValue) {
            this.value = value;
            this.expectedValue = expectedValue;
            this.hasParseError = hasParseError;
        }

        public String getValue() {
            return value;
        }

        public String getExpectedValue() {
            if (!hasParseError && (expectedValue == null || expectedValue.isEmpty())) {
                return value;
            }
            return expectedValue;
        }

        public boolean hasParseError() {
            return hasParseError;
        }
    }

    public static class FileCheckerResult {

        public final boolean isValidExtension;
        public final boolean canParse;
        private boolean uploadFailed = false;
        private boolean hasParseErrors = false;
        public List<ParseResult> parsedHeaderResult;
        public List<ParseResult> parsedFirstLineResult;
        private String parseErrorMsg = "Parse error";

        public FileCheckerResult(boolean isValidExtension, boolean canParse) {
            this.isValidExtension = isValidExtension;
            this.canParse = canParse;
        }

        public String getParseErrorMsg() {
            return parseErrorMsg;
        }

        public void setParseErrorMsg(String parseErrorMsg) {
            this.parseErrorMsg = parseErrorMsg;
        }

        public List<ParseResult> getParsedHeaderResult() {
            return parsedHeaderResult;
        }

        public List<ParseResult> getParsedFirstLineResult() {
            return parsedFirstLineResult;
        }

        public boolean hasParseErrors() {
            return hasParseErrors;
        }

        public void setHasParseErrors(boolean hasParseErrors) {
            this.hasParseErrors = hasParseErrors;
        }

        public void setParsedHeaderResult(List<ParseResult> parsedHeaderResult) {
            this.parsedHeaderResult = parsedHeaderResult;
        }

        public void setParsedFirstLineResult(List<ParseResult> parsedFirstLineResult) {
            this.parsedFirstLineResult = parsedFirstLineResult;
        }

        public boolean hasErrors() {
            return (hasParseErrors || !isValidExtension || uploadFailed);
        }

        public boolean isUploadFailed() {
            return uploadFailed;
        }

        public void setUploadFailed(boolean uploadFailed) {
            this.uploadFailed = uploadFailed;
        }

    }

    public interface FileChecker {

        public boolean isValidExtension(String extension);

        public boolean canParse(String extension);

        public boolean parse(String content, FileCheckerResult result);

        public String getSupportedFileTypes();

        public List<String> getCSVHeaderFormat();

        public List<String> getCSVContentFormat();
    }

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
            displayFileContents(file);
            return true;
        }
    };

    @UiField
    Widget uploadContainer;
    @UiField
    HeadingElement uploadDropPanelText;
    @UiField
    Form uploadForm;
    @UiField
    Button fileBrowseBtn;
    @UiField
    Button fileUploadCancelBtn;
    @UiField
    Button fileUploadStartBtn;
    @UiField
    Button fileUploadCloseBtn;
    @UiField
    FileUpload fileUploadBtn;
    @UiField
    TableElement checkFileTable;

    @UiField
    HTMLPanel fileSelectPanel;
    @UiField
    HTML uploadDropPanel;
    @UiField
    DivElement fileDropText;
    @UiField
    DivElement checkFileTableContainer;
    @UiField
    ParagraphElement supportedFileFormatsLb;
    @UiField
    TableElement csvTable;
    @UiField
    HeadingElement dropMoreLb;
    @UiField
    SpanElement additionalInfoLb;
    private boolean multiUpload = false;
    private Map<File, FileCheckerResult> filesToUpload = Maps.newLinkedHashMap();
    private BiMap<File, Element> filesToRow = HashBiMap.create();
    private Queue<File> filesInUploadQueue = Lists.newLinkedList();
    private int currentUploadCount = 0;
    private String restURL;
    private FileChecker fileChecker;


    public FileUploadWidget() {
        initWidget(binder.createAndBindUi(this));
    }

    @UiHandler("fileBrowseBtn")
    public void onClickFileBrowseBtn(ClickEvent e) {
        fileUploadBtn.getElement().<InputElement>cast().click();
    }

    @UiHandler("fileUploadBtn")
    public void onHandleFileSelect(ChangeEvent e) {
        try {
            elemental.html.InputElement input = (elemental.html.InputElement) fileUploadBtn.getElement();
            FileList fileList = input.getFiles();
            updateSelectedFileTable(fileList);
        } catch (Exception ex) {
            GWT.log("Error reading file", ex);
        }
    }

    private void updateSelectedFileTable(FileList fileList) {
        if (fileList.length() == 0)
            return;
        if (!multiUpload && filesToUpload.size() == 1)
            return;
        fileSelectPanel.addStyleName("in");
        int fileListLength = multiUpload ? fileList.getLength() : 1;
        for (int i = 0; i < fileListLength; i++) {
            File file = fileList.item(i);
            boolean fileExtOk = fileChecker.isValidExtension(file.getType());
            boolean canParse = fileChecker.canParse(file.getType());
            FileCheckerResult fileCheckerResult = new FileCheckerResult(fileExtOk, canParse);
            filesToUpload.put(file, fileCheckerResult);
            addFileToTable(file, fileCheckerResult);
            if (fileExtOk && canParse) {
                checkFileContents(file);
            }
        }
        updateFileUploadControls();
    }

    private void updateFileUploadControls() {
        boolean hasFiles = filesToUpload.size() > 0;
        fileUploadCancelBtn.setVisible(hasFiles);
        fileUploadStartBtn.setVisible(hasFiles);
        GQuery query = $("#checkStatusMsg");
        int totalCount = filesToUpload.size();
        int errorCount = countFilesWithError();
        if (errorCount == 0) {
            query.html("All added files (" + totalCount + ") are valid!");
            query.closest("div").removeClass("alert-danger").addClass("alert-success");
            fileUploadStartBtn.setEnabled(true);
        } else {
            query.html(errorCount + " out of " + totalCount + " file(s) have errors. Please fix!");
            query.closest("div").removeClass("alert-success").addClass("alert-danger");
            fileUploadStartBtn.setEnabled(false);
        }

    }

    private void addFileToTable(File file, FileCheckerResult fileCheckerResult) {
        String nameCell = "<td>" + file.getName() + "</td>";
        String sizeCell = "<td>" + String.valueOf(Math.round(file.getSize() / 1024)) + " KB</td>";
        String extCell = "<td>" + file.getType() + "</td>";
        String progressBarCell = "";
        if (fileCheckerResult.isValidExtension) {
            progressBarCell = "<td><div class=\"progress progress-striped active\" style=\"width:200px\"><div class=\"bar\" style=\"width: 0%;\"></div></div></td>";
            if (fileCheckerResult.canParse)
                nameCell = "<td><a href=\"javascript:;\">" + file.getName() + "</a></td>";
        } else {
            progressBarCell = "<td><span class=\"label label-danger\">Error</span> Filetype " + file.getType() + "not allowed</div></td>";
        }
        String cancelBtnCell = "<td><a id=\"test\" class=\"btn btn-warning\" style=\"\" aria-hidden=\"false\"><i class=\"fa fa-ban\"></i> Remove </a></td>";
        GQuery row = $("<tr>" + nameCell + sizeCell + extCell + progressBarCell + cancelBtnCell + "</tr>").appendTo($("#fileToUploadTable > tbody", this));
        Element elem = row.get(0);
        $("a", elem.getChild(0)).click(clickOnFileFunc);
        $("a", elem.getChild(4)).click(clickOnCancelFileFunc);

        filesToRow.put(file, elem);
    }

    private void updateFileInTable(File file) {
        FileCheckerResult result = filesToUpload.get(file);
        if (result.hasParseErrors()) {
            Element elem = filesToRow.get(file);
            GQuery query = $(elem);
            query.find("td:nth-child(4)").html("<span class=\"label label-danger\">Error</span> " + result.getParseErrorMsg() + "</div>");
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

    private void checkFileContents(final File file) {
        final FileCheckerResult result = filesToUpload.get(file);
        if (file == null || result == null || !result.canParse)
            return;
        FileReader reader = Browser.getWindow().newFileReader();
        Blob blob = ((HTML5Helper.ExtJsFile) file).webkitSlice(0, 100000, file.getType(), "test");
        reader.addEventListener("loadend", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                FileReader reader = (FileReader) event.getTarget();
                if (reader.getReadyState() == FileReader.DONE) {
                    String fileContent = reader.getResult().toString();
                    boolean hasParseErrors = fileChecker.parse(fileContent, result);
                    displayFileContents(file);
                    //parseAndDisplayFileContents(fileContent);
                    $("#checkFileTableHeader").html(file.getName() + ":");
                    //filesToUpload.put(file, );
                    if (hasParseErrors) {
                        updateFileInTable(file);
                        updateFileUploadControls();
                    }
                }
            }
        }, false);
        reader.readAsText(blob);
    }

    private void displayFileContents(File file) {
        FileCheckerResult result = filesToUpload.get(file);
        checkFileTableContainer.getStyle().setDisplay(Style.Display.BLOCK);
        fileDropText.getStyle().setDisplay(Style.Display.NONE);
        List<ParseResult> headerResult = result.getParsedHeaderResult();
        List<ParseResult> firstLineResult = result.getParsedFirstLineResult();
        GQuery header = $(checkFileTable).find("thead");
        GQuery body = $(checkFileTable).find("tbody tr");
        header.contents().remove();
        body.contents().remove();
        if (headerResult != null) {
            for (ParseResult parseResult : headerResult) {
                GQuery cell = $("<th>");
                if (!parseResult.hasParseError()) {
                    cell.html(parseResult.getExpectedValue()).attr("style", "color:green");
                } else {
                    cell.html(parseResult.getExpectedValue() + " [" + parseResult.getValue() + "]").attr("style", "color:red");
                }
                cell.appendTo($(header));
            }
        }
        if (firstLineResult != null) {
            for (ParseResult parseResult : firstLineResult) {
                GQuery cell = $("<td>");
                if (!parseResult.hasParseError()) {
                    if ("(Optional)".equals(parseResult.getValue())) {
                        cell.html(parseResult.getExpectedValue() + " " + parseResult.getValue()).attr("style", "color:grey");
                    } else {
                        cell.html(parseResult.getValue()).attr("style", "color:green");
                    }

                } else {
                    cell.html(parseResult.getExpectedValue() + " [" + parseResult.getValue() + "]").attr("style", "color:red");
                }
                $(body).append(cell);
            }
        }
    }

    @UiHandler("fileUploadCancelBtn")
    public void onClickFileUploadCancelBtn(ClickEvent e) {
        resetUploadForm();
    }

    public void resetUploadForm() {
        uploadForm.reset();
        fileUploadStartBtn.setVisible(false);
        fileUploadCancelBtn.setVisible(false);
        fileUploadCloseBtn.setVisible(false);
        fileBrowseBtn.setVisible(true);
        fileSelectPanel.removeStyleName("in");
        fileDropText.getStyle().setDisplay(Style.Display.BLOCK);
        checkFileTableContainer.getStyle().setDisplay(Style.Display.NONE);
        filesToUpload.clear();
        filesToRow.clear();
        filesInUploadQueue.clear();
        clearTable();
    }

    private void clearTable() {
        $("#fileToUploadTable > tbody > tr").remove();
    }

    @UiHandler("uploadDropPanel")
    public void onFileDrop(DropEvent e) {
        e.stopPropagation();
        e.preventDefault();
        HTML5Helper.ExtDataTransfer dataTransfer = (HTML5Helper.ExtDataTransfer) e.getDataTransfer();
        FileList fileList = dataTransfer.getFiles();
        updateSelectedFileTable(fileList);
    }


    @UiHandler("uploadDropPanel")
    public void onFileDragOver(DragOverEvent e) {
        e.preventDefault();
        e.stopPropagation();
    }


    @UiHandler("fileUploadStartBtn")
    public void onClickPhenotypeFileUploadStartBtn(ClickEvent e) {
        if (countFilesWithError() > 0)
            return;
        fileBrowseBtn.setVisible(false);
        fileUploadCancelBtn.setVisible(false);
        fileUploadStartBtn.setVisible(false);
        filesInUploadQueue.addAll(filesToUpload.keySet());
        fireEvent(new FileUploadStartEvent());
        startPartialUpload();
    }

    @UiHandler("fileUploadCloseBtn")
    public void onClickPhenotypeFileCloseBtn(ClickEvent e) {
        resetUploadForm();
        fireEvent(new FileUploadCloseEvent());
    }

    private int countFilesWithError() {
        return Collections2.filter(filesToUpload.values(), new Predicate<FileCheckerResult>() {

            @Override
            public boolean apply(FileCheckerResult input) {
                Preconditions.checkNotNull(input);
                return input.hasErrors();
            }
        }).size();
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
                    fireEvent(new FileUploadErrorEvent(xhr.getResponseText(), file));
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
                    fireEvent(new FileUploadErrorEvent(xhr.getResponseText(), file));
                    updateFileUploadStatus(file, false);
                }
            });
            xhr.setOnload(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    if (xhr.getStatus() != 200) {
                        fireEvent(new FileUploadErrorEvent(xhr.getResponseText(), file));
                        updateFileUploadStatus(file, false);
                    } else {
                        updateFileUploadStatus(file, true);
                        fireEvent(new FileUploadFinishedEvent(xhr.getResponseText(), file));
                    }
                }
            });
            HTML5Helper.ExtJsFormData formData = HTML5Helper.ExtJsFormData.newExtJsForm();
            formData.append("file", file, file.getName());
            xhr.open("POST", GWT.getHostPageBaseURL() + restURL);
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

    public void updateFileUploadStatus(File file, boolean isSuccess) {
        FileCheckerResult result = filesToUpload.get(file);
        result.setUploadFailed(!isSuccess);
        Element elem = filesToRow.get(file);
        GQuery query = $(elem);
        if (isSuccess) {
            query.find("td:nth-child(4)").html("<span class=\"label label-success\">FINISHED</div>");
        } else {
            query.find("td:nth-child(4)").html("<span class=\"label label-danger\">FAILED</div>");
        }
        query.find("td:nth-child(5)").hide();
        updateUploadStatus();
    }

    private void updateUploadStatus() {
        fireEvent(new FileUploadEndEvent());
        fileUploadCloseBtn.setVisible(true);
        GQuery query = $("#checkStatusMsg");
        int totalCount = filesToUpload.size();
        int errorCount = countFilesWithError();
        if (errorCount == 0) {
            query.html("All added files (" + totalCount + ") successfully uploaded!");
            query.closest("div").removeClass("alert-danger").addClass("alert-success");
        } else {
            query.html(errorCount + " out of " + totalCount + " file(s) failed to upload!");
            query.closest("div").removeClass("alert-success").addClass("alert-danger");
        }
    }

    private void updateProgressBar(File file, double max, double current) {
        Element elem = filesToRow.get(file);
        GQuery query = $(elem).find("td:nth-child(4)");
        long percentage = Math.round((current * 100.0 / max));
        query.find("div > div").css("width", percentage + "%").html(percentage + "%");
    }

    public void setMultiUpload(boolean multiUpload) {
        this.multiUpload = multiUpload;
        dropMoreLb.getStyle().setDisplay(multiUpload ? Style.Display.BLOCK : Style.Display.NONE);
    }

    public void setRestURL(String restUrl) {
        this.restURL = restUrl;
    }

    public void setFileChecker(FileChecker fileChecker) {
        this.fileChecker = fileChecker;
        updateFileFormat();
    }

    public void setAdditionalInfo(String info) {
        additionalInfoLb.setInnerHTML(info);
    }

    private void updateFileFormat() {
        GQuery query = $(csvTable);
        query.find("tbody > tr").remove();
        query.find("thead > tr").remove();
        supportedFileFormatsLb.setInnerText("any format");
        if (fileChecker == null)
            return;
        supportedFileFormatsLb.setInnerText(fileChecker.getSupportedFileTypes());
        List<String> headerFormat = fileChecker.getCSVHeaderFormat();
        if (headerFormat != null) {
            GQuery tr = $("<tr>");
            for (String item : headerFormat) {
                GQuery row = $("<th>").text(item);
                tr.append(row);
            }
            query.find("thead").append(tr);
        }
        List<String> contentFormat = fileChecker.getCSVContentFormat();
        if (contentFormat != null) {
            GQuery tr = $("<tr>");
            for (String item : contentFormat) {
                GQuery row = $("<td>").text(item);
                tr.append(row);
            }
            query.find("tbody").append(tr);
            ;
        }
    }

}