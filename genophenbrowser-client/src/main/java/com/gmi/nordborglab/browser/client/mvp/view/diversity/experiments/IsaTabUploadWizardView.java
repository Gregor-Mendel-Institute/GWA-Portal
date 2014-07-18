package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Icon;
import com.gmi.nordborglab.browser.client.csv.DefaultFileChecker;
import com.gmi.nordborglab.browser.client.editors.ExperimentEditEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeUploadDataListEditor;
import com.gmi.nordborglab.browser.client.events.FileUploadFinishedEvent;
import com.gmi.nordborglab.browser.client.events.FileUploadStartEvent;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.IsaTabUploadWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.IsaTabUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.PhenotypeUploadWizardView;
import com.gmi.nordborglab.browser.client.mvp.widgets.FileUploadWidget;
import com.gmi.nordborglab.browser.client.resources.CardCellListResources;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.card.PhenotypeUploadDataCardCell;
import com.gmi.nordborglab.browser.client.ui.cells.BooleanIconCell;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadValueProxy;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import elemental.html.File;
import org.gwtsupercsv.cellprocessor.Optional;
import org.gwtsupercsv.cellprocessor.ParseDouble;
import org.gwtsupercsv.cellprocessor.ParseLong;
import org.gwtsupercsv.cellprocessor.Trim;
import org.gwtsupercsv.cellprocessor.constraint.Equals;
import org.gwtsupercsv.cellprocessor.constraint.IsIncludedIn;
import org.gwtsupercsv.cellprocessor.ift.CellProcessor;

import java.util.List;

import static com.google.gwt.query.client.GQuery.$;

/**
 * Created by uemit.seren on 5/28/14.
 */
public class IsaTabUploadWizardView extends ViewWithUiHandlers<IsaTabUploadWizardUiHandlers> implements IsaTabUploadWizardPresenterWidget.MyView, Editor<ExperimentUploadDataProxy> {

    interface Binder extends UiBinder<Widget, IsaTabUploadWizardView> {
    }

    public interface ExperimentUploadDataEditDriver extends RequestFactoryEditorDriver<ExperimentUploadDataProxy, IsaTabUploadWizardView> {
    }


    @UiField(provided = true)
    CellList<PhenotypeUploadDataProxy> phenotypeList;
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

    @UiField
    @Path("experiment")
    ExperimentEditEditor experimentEditor;

    @UiField(provided = true)
    @Path("phenotypes")
    PhenotypeUploadDataListEditor phenotypeListEditor;
    @UiField
    Icon phenotypeValueStatusIcon;
    @UiField
    Alert phenotypeValueStatus;
    @UiField(provided = true)
    DataGrid<PhenotypeUploadValueProxy> phenotypeValuesDataGrid;
    @UiField
    CustomPager phenotypeValuePager;
    @UiField
    SpanElement studyStatus;


    private final CardCellListResources cardCellListResources;
    private final PhenotypeUploadDataCardCell phenotypeUploadDataCard;
    private final List<String> allowedExtensions = Lists.newArrayList("application/zip", "application/x-gzip");
    private final List<String> csvMineTypes = Lists.newArrayList();
    private final ExperimentUploadDataEditDriver experimentUploadDataEditDriver;
    private FileUploadWidget.FileChecker fileChecker;
    private final ChangeHandler changeHandler = new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
            getUiHandlers().updatePhenotypeData();
        }
    };

    @Inject
    public IsaTabUploadWizardView(final Binder binder,
                                  final CardCellListResources cardCellListResources,
                                  final PhenotypeUploadDataCardCell phenotypeUploadDataCard,
                                  final ExperimentUploadDataEditDriver experimentUploadEditDriver,
                                  final CustomDataGridResources dataGridResources,
                                  final OntologyManager ontologyManager,
                                  final CurrentUser currentUser
    ) {
        this.experimentUploadDataEditDriver = experimentUploadEditDriver;
        this.cardCellListResources = cardCellListResources;
        this.phenotypeUploadDataCard = phenotypeUploadDataCard;
        phenotypeValuesDataGrid = new DataGrid<PhenotypeUploadValueProxy>(50, dataGridResources);
        initDataGrid();
        this.phenotypeListEditor = new PhenotypeUploadDataListEditor(ontologyManager, currentUser.getAppData().getUnitOfMeasureList(), changeHandler);
        phenotypeList = new CellList<PhenotypeUploadDataProxy>(phenotypeUploadDataCard, cardCellListResources);
        initWidget(binder.createAndBindUi(this));
        phenotypeValuePager.setDisplay(phenotypeValuesDataGrid);
        contentContainer.showWidget(0);
        ((DeckLayoutPanel) asWidget()).showWidget(0);
        initFileUploaWidget();
        this.experimentUploadDataEditDriver.initialize(this);
    }

    private void initDataGrid() {
        final NumberFormat format = NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0);
        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, Boolean>(new BooleanIconCell()) {
            @Override
            public Boolean getValue(PhenotypeUploadValueProxy object) {
                return (!object.hasParseError() && object.isIdKnown());
            }
        });


        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, String>(new TextCell()) {
            @Override
            public String getValue(PhenotypeUploadValueProxy object) {
                if (object == null)
                    return null;
                String id = "";
                if (object.getPassportId() != null)
                    id = format.format(object.getPassportId());
                else {
                    id = object.getSourceId();
                }
                return id;
            }
        }, "ID");

        phenotypeValuesDataGrid.addColumn(new Column<PhenotypeUploadValueProxy, String>(new TextCell()) {
            @Override
            public String getValue(PhenotypeUploadValueProxy object) {
                if (object == null)
                    return null;
                if (object.getPassportId() == null) {
                    return "Could not parse id";
                }
                return object == null ? null : object.getAccessionName();

            }
        }, "Name");

        phenotypeValuesDataGrid.setColumnWidth(0, "50px");
    }


    private void initFileUploaWidget() {
        fileChecker = new DefaultFileChecker(allowedExtensions, csvMineTypes, null, null);
        fileUploadWidget.setFileChecker(fileChecker);
        fileUploadWidget.setMultiUpload(false);
        fileUploadWidget.setRestURL("/provider/isatab/upload");
    }


    @Override
    public HandlerRegistration addFileUploadFinishedEvent(FileUploadFinishedEvent.FileUploadFinishedHandler handler) {
        return fileUploadWidget.addHandler(handler, FileUploadFinishedEvent.TYPE);
    }

    @Override
    public HandlerRegistration addFileUploadStartEvent(FileUploadStartEvent.FileUploadStartHandler handler) {
        return fileUploadWidget.addHandler(handler, FileUploadStartEvent.TYPE);
    }

    @Override
    public void showFileUploadError(File file) {
        fileUploadWidget.updateFileUploadStatus(file, false);
    }

    @Override
    public void showResultPanel() {
        contentContainer.showWidget(0);
        ((DeckLayoutPanel) asWidget()).showWidget(1);
    }

    @Override
    public HasData<PhenotypeUploadDataProxy> getPhenotypeUploadList() {
        return phenotypeList;
    }

    @Override
    public void showStudyDetailPanel() {
        contentContainer.showWidget(0);
    }

    @Override
    public void showPhenotypeDetailPanel(int index) {
        contentContainer.showWidget(1);
        phenotypeListEditor.showSubEditor(index);
    }

    @Override
    public void setErrorCount(int totalCount, int count) {

        if (count == 0) {
            studyStatus.setInnerText("All " + totalCount + " phenotypes are valid");
            studyStatus.removeClassName("alert-warning");
            studyStatus.addClassName("alert-success");
        } else {
            studyStatus.setInnerText(count + " phenotypes have an error. Select a card from the left side for details");
            studyStatus.removeClassName("alert-success");
            studyStatus.addClassName("alert-warning");
        }
    }


    @Override
    public ExperimentUploadDataEditDriver getDriver() {
        return experimentUploadDataEditDriver;
    }

    @Override
    public HasData<PhenotypeUploadValueProxy> getPhenotypeValueDisplay() {
        return phenotypeValuesDataGrid;
    }

    @Override
    public void showFileUploadPanel() {
        ((DeckLayoutPanel) asWidget()).showWidget(0);
    }

    @Override
    public void resetFileUploadPanel() {
        fileUploadWidget.resetUploadForm();
    }

    @Override
    public void addColumns(List<String> header) {
        for (int i = 3; i < phenotypeValuesDataGrid.getColumnCount(); i++) {
            phenotypeValuesDataGrid.removeColumn(i);
        }

        for (int i = 0; i < header.size(); i++) {
            phenotypeValuesDataGrid.addColumn(new PhenotypeUploadWizardView.ValueColumn(i), header.get(i));
        }
    }

}