package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.github.gwtbootstrap.client.ui.Form;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeUploadWizardPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.html.File;
import elemental.html.FileList;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadWizardView extends ViewImpl implements PhenotypeUploadWizardPresenterWidget.MyView {

    interface Binder extends UiBinder<Widget, PhenotypeUploadWizardView> {
    }

    private static class ExtDataTransfer extends DataTransfer {
        protected ExtDataTransfer() {}

        public final native FileList getFiles() /*-{
            return this.files;
        }-*/;
    }

    private final Widget widget;

    @UiField HTMLPanel fileSelectPanel;

    @UiField  Button phenotypeFileUploadCancelBtn;
    @UiField Button phenotypeFileUploadStartBtn;

    @UiField  SpanElement phenotypeFileName;

    @UiField  FileUpload phenotypeFileUploadBtn;
    @UiField  Form phenotypeUploadForm;

    @UiField SpanElement phenotypeFileSize;
    @UiField SpanElement phenotypeFileExt;
    @UiField SpanElement phenotypeFileExtCheck;
    @UiField HTML phenotypeFileDropPanel;
    @UiField LayoutPanel uploadPhenotypePanel;


    @Inject
    public PhenotypeUploadWizardView(Binder binder) {
        widget = binder.createAndBindUi(this);

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
    public void onHandlePhenotypeFileSelect(ChangeEvent e)  {
        phenotypeFileName.setInnerText(phenotypeFileUploadBtn.getFilename());
        try {
            elemental.html.InputElement input = (elemental.html.InputElement)phenotypeFileUploadBtn.getElement();
            FileList fileList = input.getFiles();
            updateSelectedPhenotypeFileTable(fileList);
        }
        catch (Exception ex) {}
    }

    private void updateSelectedPhenotypeFileTable(FileList fileList) {
        File file = fileList.item(0);
        phenotypeFileName.setInnerText(file.getName());
        phenotypeFileSize.setInnerText(String.valueOf(Math.round(file.getSize()/1024)) + " KB");
        phenotypeFileExt.setInnerText(file.getType());
        phenotypeFileUploadStartBtn.setVisible(true);
        phenotypeFileUploadCancelBtn.setVisible(true);
        fileSelectPanel.addStyleName("in");
    }

    @UiHandler("phenotypeFileUploadCancelBtn")
    public void onClickPhenotypeFileUploadCancelBtn(ClickEvent e) {
        phenotypeUploadForm.reset();
        phenotypeFileUploadStartBtn.setVisible(false);
        phenotypeFileUploadCancelBtn.setVisible(false);
        fileSelectPanel.removeStyleName("in");
    }


    @UiHandler("phenotypeFileDropPanel")
    public void onPhenotypeFileDrop(DropEvent e) {
        e.stopPropagation();;
        e.preventDefault();

        ExtDataTransfer dataTransfer =  (ExtDataTransfer)e.getDataTransfer();
        FileList fileList = dataTransfer.getFiles();
        updateSelectedPhenotypeFileTable(fileList);
    }


}