package com.gmi.nordborglab.browser.client.mvp.view.diversity.tools;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.util.HTML5Helper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.ProgressEvent;
import elemental.html.File;
import elemental.html.FileList;
import elemental.xml.XMLHttpRequest;

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
    LayoutPanel uploadGWASPanel;
    @UiField
    FluidContainer gwasUploadPanel;
    @UiField
    Form gwasUploadForm;
    @UiField
    FileUpload gwasFileUploadBtn;
    @UiField
    SpanElement gwasFileName;
    @UiField
    SpanElement gwasFileSize;
    @UiField
    SpanElement gwasFileExtCheck;
    @UiField
    SpanElement gwasFileExt;
    @UiField
    ProgressBar fileUploadProgressBar;
    @UiField
    Button gwasFileUploadCancelBtn;
    @UiField
    Button gwasFileUploadStartBtn;
    @UiField
    Button gwasFileBrowseBtn;
    @UiField
    HTMLPanel fileSelectPanel;
    private File file = null;

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
        gwasFileName.setInnerText(gwasFileUploadBtn.getFilename());
        try {
            elemental.html.InputElement input = (elemental.html.InputElement)gwasFileUploadBtn.getElement();
            FileList fileList = input.getFiles();
            updateSelectedGWASFileTable(fileList);
        }
        catch (Exception ex) {}
    }

    private void updateSelectedGWASFileTable(FileList fileList) {
        file = fileList.item(0);
        gwasFileName.setInnerText(file.getName());
        gwasFileSize.setInnerText(String.valueOf(Math.round(file.getSize()/1024)) + " KB");
        gwasFileExt.setInnerText(file.getType());
        gwasFileUploadStartBtn.setVisible(true);
        gwasFileUploadCancelBtn.setVisible(true);
        fileSelectPanel.addStyleName("in");
    }

    @UiHandler("gwasFileUploadCancelBtn")
    public void onClickGWASUFileUploadCancelBtn(ClickEvent e) {
        resetUploadForm();
    }

    private void resetUploadForm() {
        gwasUploadForm.reset();
        gwasFileUploadStartBtn.setVisible(false);
        gwasFileUploadCancelBtn.setVisible(false);
        fileSelectPanel.removeStyleName("in");
        file = null;
    }

    @UiHandler("gwasFileDropPanel")
    public void onGWASFileDrop(DropEvent e) {
        e.stopPropagation();
        e.preventDefault();
        HTML5Helper.ExtDataTransfer dataTransfer = (HTML5Helper.ExtDataTransfer)e.getDataTransfer();
        FileList fileList = dataTransfer.getFiles();
        updateSelectedGWASFileTable(fileList);
    }


    @UiHandler("gwasFileUploadStartBtn")
    public void onClickPhenotypeFileUploadStartBtn(ClickEvent e) {
        if (file == null)
            return;
        final XMLHttpRequest xhr  = Browser.getWindow().newXMLHttpRequest();
        xhr.getUpload().setOnerror(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                getUiHandlers().onUploadError(xhr.getResponseText());
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
                        updateProgressBar(max, current);
                    }
                }
            }
        });
        xhr.setOnerror(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                getUiHandlers().onUploadError(xhr.getResponseText());
            }
        });
        xhr.setOnload(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (xhr.getStatus() != 200) {
                    getUiHandlers().onUploadError(xhr.getResponseText());
                }
                else {
                    getUiHandlers().onUploadFinished(xhr.getResponseText());
                }
                fileUploadProgressBar.setVisible(false);

            }
        });
        HTML5Helper.ExtJsFormData formData = HTML5Helper.ExtJsFormData.newExtJsForm();
        formData.append("file",file,file.getName());
        xhr.open("POST","/provider/gwas/upload");
        xhr.send(formData);
    }

    private void updateProgressBar(double max,double current) {
        if (!fileUploadProgressBar.isVisible())
            fileUploadProgressBar.setVisible(true);
        int percentage = (int)Math.round(max/current*100);
        fileUploadProgressBar.setPercent(percentage);
        fileUploadProgressBar.setText(percentage+" %");
        if (percentage == 100)
            fileUploadProgressBar.setVisible(true);
    }


    @Override
    public void showUploadPanel() {
        resetUploadForm();
    }

}