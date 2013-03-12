package com.gmi.nordborglab.browser.client.mvp.view.diversity.tools;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.util.HTML5Helper;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.ProgressEvent;
import elemental.html.*;
import elemental.xml.XMLHttpRequest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.google.gwt.query.client.GQuery.$;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASUploadWizardView extends ViewWithUiHandlers<GWASUploadWizardUiHandlers> implements GWASUploadWizardPresenterWidget.MyView{

    interface Binder extends UiBinder<Widget, GWASUploadWizardView> {

    }

    private final Widget widget;

    @UiField
    FluidContainer gwasUploadPanel;
    @UiField
    Form gwasUploadForm;
    @UiField
    FileUpload gwasFileUploadBtn;
    /* @UiField
    SpanElement gwasFileName;
    @UiField
    SpanElement gwasFileSize;
    @UiField
    Icon gwasFileExtCheck;
    @UiField
    SpanElement gwasFileExt;
    @UiField
    ProgressBar fileUploadProgressBar;*/
    @UiField
    Button gwasFileUploadCancelBtn;
    @UiField
    Button gwasFileUploadStartBtn;
    @UiField
    Button gwasFileBrowseBtn;
    @UiField
    HTMLPanel fileSelectPanel;
    @UiField
    HTML gwasFileDropPanel;
    @UiField
    TableElement checkFileTable;

    @UiField
    DivElement checkFileTableContainer;

    @UiField
    HeadingElement multipleFileCheckText;

    @UiField
    DivElement gwasFileDropText;
    @UiField
    Button gwasFileUploadCloseBtn;
    private File file = null;
    private boolean multipleUpload=true;

    private List<String> headerColumns = ImmutableList.of("chr","position","pvalue|score","maf","mac","GVE");
    private Map<File,Boolean> filesToUpload = Maps.newLinkedHashMap();
    private Queue<File> filesInUploadQueue = Lists.newLinkedList();

    private BiMap<File,Element> filesToRow =  HashBiMap.create();
    private int currentUploadCount = 0;
    private String restURL = "provider/gwas/upload";

    /*@UiField
    DivElement gwasFileExtError;
    @UiField
    DivElement gwasFileExtOk;*/

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
    public GWASUploadWizardView(final Binder binder) {
        widget = binder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("gwasFileBrowseBtn")
    public void onClickGWASFileBrowseBtn(ClickEvent e) {
        gwasFileUploadBtn.getElement().<InputElement>cast().click();
    }

    @UiHandler("gwasFileUploadBtn")
    public void onHandleGWASFileSelect(ChangeEvent e) {
        //gwasFileName.setInnerText(gwasFileUploadBtn.getFilename());
        try {
            elemental.html.InputElement input = (elemental.html.InputElement)gwasFileUploadBtn.getElement();
            FileList fileList = input.getFiles();
            updateSelectedGWASFileTable(fileList);
        }
        catch (Exception ex) {}
    }

    private void updateSelectedGWASFileTable(FileList fileList) {
        if (fileList.length() == 0)
            return;
        if (!multipleUpload && filesToUpload.size() == 1 )
            return;
        fileSelectPanel.addStyleName("in");
        int fileListLength = multipleUpload ? fileList.getLength() : 1;
        for (int i =0;i<fileListLength;i++) {
            File file = fileList.item(i);

            boolean fileExtOk =  checkFileExtOk(file);
            boolean isParseOk = true;
            filesToUpload.put(file,(fileExtOk & isParseOk));
            addFileToTable(file,fileExtOk,isParseOk);
            if (fileExtOk && file.getType().equals("text/csv")) {
                checkFileContents(file);
            }
        }
        updateFileUploadControls();
    }

    private void updateFileUploadControls() {
        boolean hasFiles = filesToUpload.size() > 0;
        gwasFileUploadCancelBtn.setVisible(hasFiles);
        gwasFileUploadStartBtn.setVisible(hasFiles);
        GQuery query = $("#checkStatusMsg");
        int totalCount = filesToUpload.size();
        int errorCount = countFilesWithError();
        if (errorCount == 0) {
            query.html("All added files ("+totalCount+") are valid!");
            query.closest("div").removeClass("alert-error").addClass("alert-success");
            gwasFileUploadStartBtn.setEnabled(true);
        } else {
            query.html(errorCount + " out of " + totalCount+ " file(s) have errors. Please fix!");
            query.closest("div").removeClass("alert-success").addClass("alert-error");
            gwasFileUploadStartBtn.setEnabled(false);
        }

    }

    private void addFileToTable(File file, boolean isExtOk, boolean isParseOk) {
        String nameCell = "<td>"+file.getName()+"</td>";
        String sizeCell = "<td>"+String.valueOf(Math.round(file.getSize()/1024)) + " KB</td>";
        String extCell =  "<td>"+file.getType()+"</td>";
        String progressBarCell = "";
        if (isExtOk) {
            progressBarCell = "<td><div class=\"progress progress-striped active\" style=\"width:200px\"><div class=\"bar\" style=\"width: 0%;\"></div></div></td>";
            if (file.getType().equals("text/csv"))
                nameCell = "<td><a href=\"javascript:;\">"+file.getName()+"</a></td>";
        }
        else {
            progressBarCell = "<td><span class=\"label label-important\">Error</span> Filetype not allowed</div></td>";
        }
        String cancelBtnCell = "<td><a href=\"javascript:;\" class=\"btn btn-warning\" style=\"\" aria-hidden=\"false\"><i class=\"icon-ban-circle\"></i> Remove </a></td>";
        GQuery row = $("<tr>"+nameCell+sizeCell+extCell + progressBarCell+cancelBtnCell+"</tr>").appendTo($("#fileToUploadTable > tbody:last"));
        Element elem = row.get(0);
        $("a",elem.getChild(0)).bind(com.google.gwt.user.client.Event.ONCLICK,clickOnFileFunc);
        $("a",elem.getChild(4)).bind(com.google.gwt.user.client.Event.ONCLICK, clickOnCancelFileFunc);
        filesToRow.put(file, elem);
    }

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

    private void checkFileContents(final File file) {
        if (file == null || !file.getType().equals("text/csv"))
            return;
        FileReader reader = Browser.getWindow().newFileReader();
        Blob blob = ((HTML5Helper.ExtJsFile)file).webkitSlice(0, 100,file.getType(),"test");
        reader.addEventListener("loadend", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                FileReader reader = (FileReader)event.getTarget();
                if (reader.getReadyState() ==  FileReader.DONE) {
                    String fileContent = reader.getResult().toString();
                    boolean isParseOk = parseAndDisplayFileContents(fileContent);
                    $("#checkFileTableHeader").html(file.getName()+":");
                    filesToUpload.put(file,isParseOk);
                    if (!isParseOk) {
                        updateFileInTable(file);
                        updateFileUploadControls();
                    }

                }
            }
        }, false);
        reader.readAsText(blob);
    }

    private boolean parseAndDisplayFileContents(String fileContent) {
        //TODO regular expression
        String[] lines = fileContent.split("\n");
        if (lines.length == 1)
            lines = fileContent.split("\r");
        String header=null;
        String firstLine = null;
        if (lines.length > 0)
            header = lines[0];
        if (lines.length > 1)
            firstLine = lines[1];
        boolean isHeaderOk = checkHeader(header);
        boolean isFirstLineOk = checkFirstLine(firstLine);
        checkFileTableContainer.getStyle().setDisplay(Style.Display.BLOCK);
        gwasFileDropText.getStyle().setDisplay(Style.Display.NONE);
        return (isHeaderOk & isFirstLineOk);
        //gwasFileUploadStartBtn.setVisible((isHeaderOk & isFirstLineOk));
    }

    private boolean checkFirstLine(String firstLine) {
        boolean isOk = true;
        String[] values = new String[0];
        if (firstLine != null)
             values = firstLine.split(",");

        Element[] elements = $(checkFileTable).find("tbody tr td").elements();
        for (int i =0;i<6;i++) {

            if (i == 0) {
                if (!checkNotMissingAndLong(values,i,elements[i]))
                    isOk = false;
            }
            else if (i == 1) {
                if (!checkNotMissingAndLong(values, i, elements[i]))
                    isOk = false;
            }
            else if (i == 2) {
                if (!checkNotMissingAndDouble(values, i, elements[i]))
                    isOk = false;
            }
            else if (i == 3) {
                if (!checkOptionalAndDouble(values, i, elements[i]))
                    isOk = false;
            }
            else if (i == 4) {
                if (!checkOptionalAndLong(values, i, elements[i]))
                    isOk = false;
            }
            else if (i==5) {
                if (!checkOptionalAndDouble(values, i, elements[i]))
                    isOk = false;
            }
        }

        return isOk;
    }

    private boolean checkNotMissingAndLong(String[] values,int i,Element elem) {
        boolean isOk = true;
        if (checkMissing(values,i)) {
            elem.setInnerText("MISSING");
            elem.getStyle().setColor("red");
            isOk = false;
        }
        else if (!checkLong(values[i])) {
            elem.setInnerText(values[i] + " [LONG]");
            elem.getStyle().setColor("red");
            isOk = false;
        }
        else {
            elem.setInnerText(values[i]);
            elem.getStyle().setColor("green");
        }
        return isOk;
    }

    private boolean checkNotMissingAndDouble(String[] values,int i,Element elem) {
        boolean isOk = true;
        if (checkMissing(values,i)) {
            elem.setInnerText("MISSING");
            elem.getStyle().setColor("red");
            isOk = false;
        }
        else if (!checkDouble(values[i])) {
            elem.setInnerText(values[i]+" [LONG]");
            elem.getStyle().setColor("red");
            isOk = false;
        }
        else {
            elem.setInnerText(values[i]);
            elem.getStyle().setColor("green");
        }
        return isOk;
    }

    private boolean checkOptionalAndDouble(String[] values,int i,Element elem) {
        boolean isOk = true;
        if (checkMissing(values,i)) {
            elem.setInnerText("(Optional)");
            elem.getStyle().setColor("grey");
            isOk = true;
        }
        else if (!checkDouble(values[i])) {
            elem.setInnerText(values[i]+" [LONG]");
            elem.getStyle().setColor("red");
            isOk = false;
        }
        else {
            elem.setInnerText(values[i]);
            elem.getStyle().setColor("green");
        }
        return isOk;
    }

    private boolean checkOptionalAndLong(String[] values,int i,Element elem) {
        boolean isOk = true;
        if (checkMissing(values,i)) {
            isOk = true;
        }
        else if (!checkDouble(values[i])) {
            elem.setInnerText(values[i]+" [LONG]");
            elem.getStyle().setColor("red");
            isOk = false;
        }
        else {
            elem.setInnerText(values[i]);
            elem.getStyle().setColor("green");
        }
        return isOk;
    }

    private boolean checkLong(String value) {
         boolean isOk = true;
         try {
             Long.parseLong(value);
         }
         catch (Exception e) {

         }
        return isOk;
    }

    private boolean checkDouble(String value) {
        boolean isOk = true;
        try {
            Double.parseDouble(value);
        }
        catch (Exception e) {

        }
        return isOk;
    }

    private boolean checkMissing(String[] values,int i) {
        return i+1 > values.length;
    }

    private boolean checkHeader(String header) {

        String[] columns = new String[0];
        if (header != null)
            columns = header.split(",");
        Element[] elements = $(checkFileTable).find("thead th").elements();
        boolean isOk = true;
        for (int i =0;i<headerColumns.size();i++) {
            String headerColumn = headerColumns.get(i);
            if (i+1 > columns.length) {
                elements[i].setInnerText(headerColumn+ " [MISSING]");
                elements[i].getStyle().setColor("red");
                isOk = false;
            }
            else {
                String[] parts = headerColumn.split("\\|");
                if (columns[i].equals(parts[0])) {
                    elements[i].setInnerText(headerColumn);
                    elements[i].getStyle().setColor("green");
                }
                else if (parts.length == 2 && columns[i].equals(parts[1].trim())) {
                    elements[i].setInnerText(headerColumn);
                    elements[i].getStyle().setColor("green");
                }
                else {
                    elements[i].getStyle().setColor("red");
                    elements[i].setInnerText("\""+columns[i]+"\" [\""+headerColumn+"\"]");
                    isOk = false;
                }
            }
        }
        return isOk;
    }

    private boolean checkFileExtOk(File file) {
        String fileExt = file.getType();
        if (fileExt.equals("application/x-hdf") || fileExt.equals("text/csv"))
            return true;

        return false;
    }

    @UiHandler("gwasFileUploadCancelBtn")
    public void onClickGWASUFileUploadCancelBtn(ClickEvent e) {
        resetUploadForm();
    }

    private void resetUploadForm() {
        gwasUploadForm.reset();
        gwasFileUploadStartBtn.setVisible(false);
        gwasFileUploadCancelBtn.setVisible(false);
        gwasFileUploadCloseBtn.setVisible(false);
        gwasFileBrowseBtn.setVisible(true);
        fileSelectPanel.removeStyleName("in");
        gwasFileDropText.getStyle().setDisplay(Style.Display.BLOCK);
        checkFileTableContainer.getStyle().setDisplay(Style.Display.NONE);
        filesToUpload.clear();
        filesToRow.clear();
        filesInUploadQueue.clear();
        clearTable();
    }

    private void clearTable() {
        $("#fileToUploadTable > tbody > tr").remove();
    }

    @UiHandler("gwasFileDropPanel")
    public void onGWASFileDrop(DropEvent e) {
        e.stopPropagation();
        e.preventDefault();
        HTML5Helper.ExtDataTransfer dataTransfer = (HTML5Helper.ExtDataTransfer)e.getDataTransfer();
        FileList fileList = dataTransfer.getFiles();
        updateSelectedGWASFileTable(fileList);
    }


    @UiHandler("gwasFileDropPanel")
    public void onGWASFileDragOver(DragOverEvent e) {
        e.preventDefault();
        e.stopPropagation();
    }


    @UiHandler("gwasFileUploadStartBtn")
    public void onClickPhenotypeFileUploadStartBtn(ClickEvent e) {
        if (countFilesWithError() >0)
            return;
        gwasFileBrowseBtn.setVisible(false);
        gwasFileUploadCancelBtn.setVisible(false);
        gwasFileUploadStartBtn.setVisible(false);
        filesInUploadQueue.addAll(filesToUpload.keySet());
        startPartialUpload();
    }

    @UiHandler("gwasFileUploadCloseBtn")
    public void onClickPhenotypeFileCloseBtn(ClickEvent e) {
        resetUploadForm();
        getUiHandlers().onClose();
    }

    private int countFilesWithError() {
        return Collections2.filter(filesToUpload.values(),new Predicate<Boolean>() {

            @Override
            public boolean apply(@Nullable Boolean input) {
                return !input;
            }
        }).size();
    }

    private void startPartialUpload() {
        int remainingUploadSlots = 3-currentUploadCount;
        if (remainingUploadSlots > filesInUploadQueue.size())
            remainingUploadSlots = filesInUploadQueue.size();
        for (int i=0;i<remainingUploadSlots;i++) {
            final XMLHttpRequest xhr  = Browser.getWindow().newXMLHttpRequest();
            final File file = filesInUploadQueue.poll();
            xhr.getUpload().setOnerror(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    getUiHandlers().onUploadError(xhr.getResponseText());
                    updateFileUploadStatus(file,false);
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
                            updateProgressBar(file,max, current);
                        }
                    }
                }
            });
            xhr.setOnerror(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    getUiHandlers().onUploadError(xhr.getResponseText());
                    updateFileUploadStatus(file,false);
                }
            });
            xhr.setOnload(new EventListener() {
                @Override
                public void handleEvent(Event event) {
                    deccCurrentUploadCount();
                    if (xhr.getStatus() != 200) {
                        getUiHandlers().onUploadError(xhr.getResponseText());
                        updateFileUploadStatus(file,false);
                    }
                    else {
                        getUiHandlers().onUploadFinished(xhr.getResponseText());
                        updateFileUploadStatus(file,true);
                    }
                }
            });
            HTML5Helper.ExtJsFormData formData = HTML5Helper.ExtJsFormData.newExtJsForm();
            formData.append("file",file,file.getName());
            xhr.open("POST",GWT.getHostPageBaseURL()+restURL);
            xhr.send(formData);
            currentUploadCount +=1;
        }
        if (remainingUploadSlots == 0 && filesInUploadQueue.size() == 0) {
            updateUploadStatus();
        }
    }

    private void deccCurrentUploadCount() {
        if (currentUploadCount > 1)
            currentUploadCount -=1;
        startPartialUpload();
    }

    private void updateFileUploadStatus(File file,boolean isSuccess) {
        Element elem = filesToRow.get(file);
        GQuery query = $(elem);
        if (isSuccess) {
            query.find("td:nth-child(4)").html("<span class=\"label label-success\">FINISHED</div>");
        }
        else {
            filesToUpload.put(file,false);
            query.find("td:nth-child(4)").html("<span class=\"label label-important\">FAILED</div>");
        }
        query.find("td:nth-child(5)").hide();
        updateUploadStatus();
    }

    private void updateUploadStatus() {
        gwasFileUploadCloseBtn.setVisible(true);
        GQuery query = $("#checkStatusMsg");
        int totalCount = filesToUpload.size();
        int errorCount = countFilesWithError();
        if (errorCount == 0) {
            query.html("All added files ("+totalCount+") successfully uploaded!");
            query.closest("div").removeClass("alert-error").addClass("alert-success");
        } else {
            query.html(errorCount + " out of " + totalCount+ " file(s) failed to upload!");
            query.closest("div").removeClass("alert-success").addClass("alert-error");
        }
    }

    private void updateProgressBar(File file,double max,double current) {
        Element elem = filesToRow.get(file);
        GQuery query = $(elem).find("td:nth-child(4)");
        int percentage = (int)Math.round(max/current*100);
        query.find("div > div").css("width",percentage+"%").html(percentage+"%");
    }


    @Override
    public void showUploadPanel() {
        resetUploadForm();
    }

    @Override
    public void setmultipleUpload(boolean multipleUpload) {
        this.multipleUpload = multipleUpload;
    }

    @Override
    public void setRestURL(String restUrl) {
        this.restURL = restUrl;
    }

}