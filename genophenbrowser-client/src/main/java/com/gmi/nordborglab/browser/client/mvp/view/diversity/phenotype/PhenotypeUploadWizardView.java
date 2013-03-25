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
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.i18n.client.NumberFormat;
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
import elemental.html.File;
import elemental.html.FileList;
import elemental.xml.XMLHttpRequest;

import java.util.List;

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

    private static class ValueColumn extends Column<PhenotypeUploadValueProxy,String> {

        private final int headerIx;
        private ValueColumn(int headerIx) {
            super(new TextCell());
            this.headerIx = headerIx;
        }

        @Override
        public String getValue(PhenotypeUploadValueProxy object) {
            return object == null ? null :  object.getValues().get(headerIx);
        }
    }
    public interface PhenotypeDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeEditEditor> {}

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
    @UiField LayoutPanel phenotypeValuePanel;
    @UiField
    ProgressBar fileUploadProgressBar;
    @UiField
    FluidContainer phenotypeUploadPanel;
    @UiField
    CustomPager phenotypeValuePager;
    @UiField(provided=true)
    DataGrid<PhenotypeUploadValueProxy> phenotypeValuesDataGrid;

    @UiField Alert phenotypeValueStatus;
    @UiField
    PhenotypeEditEditor phenotypeEditor;

    private final PhenotypeDriver driver;


    private File file = null;
    @Inject
    public PhenotypeUploadWizardView(final Binder binder,
                                     final CustomDataGridResources dataGridResources,final PhenotypeDriver driver) {
        this.driver = driver;
        phenotypeValuesDataGrid = new DataGrid<PhenotypeUploadValueProxy>(50,dataGridResources);
        initCellTable();
        widget = binder.createAndBindUi(this);
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel,false);
        phenotypeValuePager.setDisplay(phenotypeValuesDataGrid);
        driver.initialize(phenotypeEditor);
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
                    id =  object.getPassportId();
                else {
                    try {
                       id = Long.parseLong(object.getSourceId());
                    }
                    catch (Exception e) {

                    }
                }
                return id;
            }
        },"ID");

        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, String>(new TextCell()) {
            @Override
            public String getValue(PhenotypeUploadValueProxy object) {
                return object == null ? null : object.getAccessionName();
            }
        },"Name");
    }

    private <C> void addColumn(Column<PhenotypeUploadValueProxy,C> column,String header) {
        phenotypeValuesDataGrid.addColumn(column,header);
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
        file = fileList.item(0);
        phenotypeFileName.setInnerText(file.getName());
        phenotypeFileSize.setInnerText(String.valueOf(Math.round(file.getSize()/1024)) + " KB");
        phenotypeFileExt.setInnerText(file.getType());
        phenotypeFileUploadStartBtn.setVisible(true);
        phenotypeFileUploadCancelBtn.setVisible(true);
        fileSelectPanel.addStyleName("in");
    }

    @UiHandler("phenotypeFileUploadCancelBtn")
    public void onClickPhenotypeFileUploadCancelBtn(ClickEvent e) {
        resetUploadForm();
    }

    private void resetUploadForm() {
        phenotypeUploadForm.reset();
        phenotypeFileUploadStartBtn.setVisible(false);
        phenotypeFileUploadCancelBtn.setVisible(false);
        fileSelectPanel.removeStyleName("in");
        file = null;
    }

    @UiHandler("phenotypeFileDropPanel")
    public void onPhenotypeFileDrop(DropEvent e) {
        e.stopPropagation();;
        e.preventDefault();

        HTML5Helper.ExtDataTransfer dataTransfer =  (HTML5Helper.ExtDataTransfer)e.getDataTransfer();
        FileList fileList = dataTransfer.getFiles();
        updateSelectedPhenotypeFileTable(fileList);
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
        if (file == null)
            return;
        getUiHandlers().startUpload();
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
        xhr.open("POST", GWT.getHostPageBaseURL()+"provider/phenotype/upload");
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
    public void showPhenotypeValuePanel(PhenotypeUploadDataProxy data) {
        resetUploadForm();
        String message = "All phentoype values successfully parsed";
        AlertType messageType = AlertType.SUCCESS;
        if (data.getErrorValueCount() > 0) {
            messageType = AlertType.ERROR;
            message = data.getErrorValueCount()+" of "+data.getPhenotypeUploadValues().size() + " phenotype values haven an error.";
        }
        if (data.getErrorMessage() != null && !data.getErrorMessage().equals("")) {
            messageType = AlertType.ERROR;
            message = data.getErrorMessage();
        }
        phenotypeValueStatus.setType(messageType);
        phenotypeValueStatus.setText(message);
        phenotypeValueStatus.setVisible(true);

        uploadPhenotypePanel.setWidgetVisible(phenotypeUploadPanel, false);
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel,true);
    }

    @Override
    public void showPhenotypeUploadPanel() {
        resetUploadForm();
        phenotypeValueStatus.setVisible(false);
        phenotypeValueStatus.setText("");
        uploadPhenotypePanel.setWidgetVisible(phenotypeValuePanel,false);
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
        for (int i=3;i<phenotypeValuesDataGrid.getColumnCount();i++) {
            phenotypeValuesDataGrid.removeColumn(i);
        }

        for (int i = 0;i<valueColumns.size();i++) {
            phenotypeValuesDataGrid.addColumn(new ValueColumn(i),valueColumns.get(i));
        }
    }


    @Override
    public void showConstraintViolations() {

    }

    @Override
    public PhenotypeDriver getDriver() {
        return driver;
    }


    public final native void logPhenotypeValue(String value) /*-{
        return $wnd.console.log(value);
    }-*/;



}