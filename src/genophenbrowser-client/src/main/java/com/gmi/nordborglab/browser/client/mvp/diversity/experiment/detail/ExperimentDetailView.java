package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail;

import com.gmi.nordborglab.browser.client.editors.ExperimentDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.ExperimentEditEditor;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.PhaseAnimation;
import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.browser.shared.util.ConstEnums.ONTOLOGY_TYPE;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.PieChart;
import com.googlecode.gwt.charts.client.corechart.PieChartOptions;
import com.googlecode.gwt.charts.client.options.ChartArea;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendAlignment;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.SimpleCallback;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentDetailView extends ViewWithUiHandlers<ExperimentDetailUiHandlers> implements
        ExperimentDetailPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, ExperimentDetailView> {
    }

    public interface ExperimentEditDriver extends RequestFactoryEditorDriver<ExperimentProxy, ExperimentEditEditor> {
    }

    public interface ExperimentDisplayDriver extends RequestFactoryEditorDriver<ExperimentProxy, ExperimentDisplayEditor> {
    }

    private ExperimentEditEditor experimentEditEditor = new ExperimentEditEditor();
    @UiField(provided = true)
    ExperimentDisplayEditor experimentDisplayEditor;
    @UiField
    Anchor edit;
    @UiField
    Anchor delete;
    @UiField
    Anchor share;
    @UiField(provided = true)
    PublicationResponsiveDataGrid publicationDataGrid;
    @UiField
    TextBox doiTb;
    @UiField
    Form addDOIForm;
    @UiField
    Tooltip shareTooltip;
    @UiField
    SpanElement phenotypeCountLb;
    @UiField
    SpanElement analysisCountLb;
    @UiField
    PieChart ontologyChart;
    @UiField
    org.gwtbootstrap3.client.ui.ButtonGroup ontologyTypeBtnGrp;
    @UiField
    HTMLPanel actionBarPanel;
    @UiField
    AnchorListItem navLinkIsaTab;
    private final ExperimentEditDriver experimentEditDriver;
    private final ExperimentDisplayDriver experimentDisplayDriver;
    private final MainResources resources;
    private Modal permissionPopUp = new Modal();
    private ModalBody permissionPopUpContent = new ModalBody();

    private boolean layoutScheduled = false;
    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };
    private Modal editPopup = new Modal();
    private DialogOptions deleteOptions = DialogOptions.newOptions("Do you really want to delete the study?");

    private ONTOLOGY_TYPE currentOntologyType = ConstEnums.ONTOLOGY_TYPE.TRAIT;
    private Map<ONTOLOGY_TYPE, DataTable> ontology2Map = new HashMap<ONTOLOGY_TYPE, DataTable>();

    @Inject
    public ExperimentDetailView(final Binder binder,
                                final ExperimentEditDriver experimentEditDriver,
                                final ExperimentDisplayDriver experimentDisplayDriver,
                                final MainResources resources,
                                final CustomDataGridResources customDataGridResources,
                                final ExperimentDisplayEditor experimentDisplayEditor) {
        this.resources = resources;
        this.experimentDisplayEditor = experimentDisplayEditor;
        publicationDataGrid = new PublicationResponsiveDataGrid(50, customDataGridResources, new ActionCell.Delegate<PublicationProxy>() {
            @Override
            public void execute(PublicationProxy object) {
                getUiHandlers().onDeletePublication(object);
            }
        });
        widget = binder.createAndBindUi(this);
        permissionPopUp.add(permissionPopUpContent);
        bindSlot(ExperimentDetailPresenter.SLOT_PERMISSION, permissionPopUpContent);

        this.experimentEditDriver = experimentEditDriver;
        this.experimentDisplayDriver = experimentDisplayDriver;
        this.experimentDisplayDriver.initialize(experimentDisplayEditor);
        this.experimentEditDriver.initialize(experimentEditEditor);

        permissionPopUp.setTitle("Permissions");
        //permissionPopUp.setMaxHeigth("700px");
        permissionPopUp.setClosable(false);
        permissionPopUp.setDataKeyboard(false);
        //initDataGrid();

        editPopup.setDataBackdrop(ModalBackdrop.STATIC);
        editPopup.setClosable(true);
        editPopup.setTitle("Edit study");
        Button cancelEditBtn = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onCancel();
            }
        });
        cancelEditBtn.setType(ButtonType.DEFAULT);
        Button saveEditBtn = new Button("Save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onSave();
            }
        });
        saveEditBtn.setType(ButtonType.PRIMARY);
        ModalFooter footer = new ModalFooter();
        footer.add(cancelEditBtn);
        footer.add(saveEditBtn);
        ModalBody modalBody = new ModalBody();
        modalBody.add(experimentEditEditor);
        editPopup.add(modalBody);
        editPopup.add(footer);

        deleteOptions.setTitle("Delete study");
        deleteOptions.addButton("Cancel");
        deleteOptions.addButton("Delete", ButtonType.DANGER.getCssName(), new SimpleCallback() {
            @Override
            public void callback() {
                getUiHandlers().onConfirmDelete();
            }
        });
        actionBarPanel.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        delete.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onDelete();
            }
        });

        share.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onShare();
            }
        });
        edit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onEdit();
            }
        });
    }

    private void initDataGrid() {

        HTML emptyWidget = new HTML("<h1 style=\"line-height:220px;color: #ccc;\">No Publications added</h1>");
        publicationDataGrid.setEmptyTableWidget(emptyWidget);
        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getFirstAuthor();
            }
        }, "Author");

        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getTitle();
            }
        }, "Title");

        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return String.valueOf(object.getPubDate().getYear());
            }
        }, "Year");
        publicationDataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getJournal();
            }
        }, "Journal");
        publicationDataGrid.addColumn(new Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam>(new HyperlinkCell(true)) {
            @Override
            public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(PublicationProxy object) {
                return new HyperlinkPlaceManagerColumn.HyperlinkParam(object.getDOI(), object.getURL());
            }
        }, "Doi");
        publicationDataGrid.addColumn(new IdentityColumn<PublicationProxy>(new EntypoIconActionCell<PublicationProxy>("e_icon-trash;", new ActionCell.Delegate<PublicationProxy>() {
            @Override
            public void execute(PublicationProxy object) {
                getUiHandlers().onDeletePublication(object);
            }
        })) {
            @Override
            public PublicationProxy getValue(PublicationProxy object) {
                return object;
            }
        }, "");
        publicationDataGrid.setColumnWidth(0, "100px");
        publicationDataGrid.setColumnWidth(2, "60px");
        publicationDataGrid.setColumnWidth(3, "80px");
        publicationDataGrid.setColumnWidth(5, "40px");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public ExperimentEditDriver getExperimentEditDriver() {
        return experimentEditDriver;
    }

    @Override
    public void showPermissionPanel(boolean show) {
        if (show)
            permissionPopUp.show();
        else
            permissionPopUp.hide();
    }

    @Override
    public HasData<PublicationProxy> getPublicationDisplay() {
        return publicationDataGrid;
    }

    @Override
    public ExperimentDisplayDriver getExperimentDisplayDriver() {
        return experimentDisplayDriver;
    }


    @UiHandler("addPublication")
    public void onClickAddPublication(ClickEvent e) {
        if (doiTb.getText().equals(""))
            return;
        getUiHandlers().queryDOI(doiTb.getText());
    }

    private void forceLayout() {
        publicationDataGrid.onResize();
    }

    @Override
    public void scheduledLayout() {
        if (!layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    @Override
    public void phaseInPublication(PublicationProxy publication, ProvidesKey<PublicationProxy> providesKey) {
        (new PhaseAnimation.DataGridPhaseAnimation<PublicationProxy>(publicationDataGrid, publication, providesKey)).run();
    }

    @Override
    public HasText getDOIText() {
        return doiTb;
    }



    @Override
    public void showEditPopup(boolean show) {
        if (show)
            editPopup.show();
        else
            editPopup.hide();
    }

    @Override
    public void showDeletePopup() {
        Bootbox.dialog(deleteOptions);
    }

    @Override
    public void showShareBtn(boolean show) {
        share.setVisible(show);
    }

    @Override
    public void setShareTooltip(String toopltipMsg, IconType icon) {
        shareTooltip.setTitle(toopltipMsg);
        share.setIcon(icon);
        shareTooltip.reconfigure();
    }

    @Override
    public void showActionBtns(boolean show) {
        edit.setVisible(show);
        delete.setVisible(show);
    }

    @Override
    public void displayStats(List<FacetProxy> stats, int numberOfPhenotypes, long numberOfAnalysis) {
        initDataTables(stats);
        drawDataTable();
        phenotypeCountLb.setInnerText(String.valueOf(numberOfPhenotypes));
        analysisCountLb.setInnerText(String.valueOf(numberOfAnalysis));
    }

    private void drawDataTable() {
        ontologyChart.draw(ontology2Map.get(currentOntologyType), getChartOptions());
    }

    private PieChartOptions getChartOptions() {
        PieChartOptions options = PieChartOptions.create();
        options.setHeight(340);
        ChartArea area = ChartArea.create();
        area.setLeft(0);
        area.setTop(0);
        area.setHeight("100%");
        area.setWidth("100%");
        options.setChartArea(area);
        Legend legend = Legend.create();
        legend.setAligment(LegendAlignment.CENTER);
        legend.setPosition(LegendPosition.RIGHT);
        options.setLegend(legend);
        return options;
    }

    private void initDataTables(List<FacetProxy> stats) {
        ontology2Map.clear();
        ontology2Map.put(ConstEnums.ONTOLOGY_TYPE.TRAIT, getDataTableFromStats(stats.get(0)));
        ontology2Map.put(ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT, getDataTableFromStats(stats.get(1)));
    }

    private DataTable getDataTableFromStats(FacetProxy stat) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, stat.getName());
        dataTable.addColumn(ColumnType.NUMBER, "count");
        dataTable.addRows(stat.getTerms().size());
        for (int i = 0; i < stat.getTerms().size(); i++) {
            FacetTermProxy term = stat.getTerms().get(i);
            dataTable.setValue(i, 0, term.getTerm());
            dataTable.setValue(i, 1, term.getValue());
        }
        return dataTable;
    }

    @UiHandler("traitTypeBtn")
    public void onClickTraitTypeBtn(ClickEvent e) {
        if (currentOntologyType != ConstEnums.ONTOLOGY_TYPE.TRAIT) {
            currentOntologyType = ConstEnums.ONTOLOGY_TYPE.TRAIT;
            drawDataTable();
        }
    }

    @UiHandler("envTypeBtn")
    public void onClickEnvironmentTypeBtn(ClickEvent e) {
        if (currentOntologyType != ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT) {
            currentOntologyType = ConstEnums.ONTOLOGY_TYPE.ENVIRONMENT;
            drawDataTable();
        }
    }

    @Override
    public void setDownloadLink(String url) {
        navLinkIsaTab.setHref(url);
    }
}
