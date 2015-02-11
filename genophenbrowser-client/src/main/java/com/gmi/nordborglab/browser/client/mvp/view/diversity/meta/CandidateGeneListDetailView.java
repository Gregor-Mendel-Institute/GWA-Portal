package com.gmi.nordborglab.browser.client.mvp.view.diversity.meta;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Tooltip;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.editors.CandidateGeneListDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.CandidateGeneListEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.CandidateGeneListDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.PhaseAnimation;
import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.PieChart;
import com.googlecode.gwt.charts.client.corechart.PieChartOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.09.13
 * Time: 12:05
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListDetailView extends ViewWithUiHandlers<CandidateGeneListDetailUiHandlers> implements CandidateGeneListDetailPresenter.MyView {

    interface Binder extends UiBinder<Widget, CandidateGeneListDetailView> {

    }

    public static class ActionHasCell implements HasCell<GeneProxy, GeneProxy> {


        private final Cell<GeneProxy> cell;

        private ActionHasCell(final Cell<GeneProxy> cell) {
            this.cell = cell;
        }

        @Override
        public Cell<GeneProxy> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<GeneProxy, GeneProxy> getFieldUpdater() {
            return null;
        }

        @Override
        public GeneProxy getValue(GeneProxy object) {
            return object;
        }
    }

    public interface CandidateGeneListDisplayDriver extends RequestFactoryEditorDriver<CandidateGeneListProxy, CandidateGeneListDisplayEditor> {
    }

    private final Widget widget;

    @UiField(provided = true)
    DataGrid<GeneProxy> genesDataGrid;

    @UiField
    CustomPager genesPager;

    @UiField(provided = true)
    CandidateGeneListDisplayEditor candidateGeneListDisplayEditor;
    private CandidateGeneListEditEditor candidateGeneListEditEditor = new CandidateGeneListEditEditor();

    @UiField(provided = true)
    Typeahead searchGeneTa;
    @UiField
    TextBox searchGeneTb;
    @UiField
    Button addGeneBtn;

    @UiField
    Button shareBtn;
    @UiField
    Button deleteBtn;
    @UiField
    Button editBtn;
    @UiField
    Tooltip shareTooltip;
    @UiField
    PieChart annotationPieChart;
    @UiField
    PieChart chrPieChart;
    @UiField
    PieChart strandPieChart;
    @UiField
    FileUpload fileUpload;
    @UiField
    FormPanel formPanel;
    @UiField
    Button uploadBtn;
    @UiField
    SimpleLayoutPanel enrichmentContainer;
    @UiField
    HTMLPanel actionBarPanel;
    @UiField
    SimplePanel facetContainer;
    private int minCharSize = 3;
    private final CandidateGeneListDisplayDriver candidateGeneListDisplayDriver;
    private final CandidateGeneListView.CandidateGeneListEditDriver candidateGeneListEditDriver;
    private final BiMap<CandidateGeneListDetailPresenter.STATS, PieChart> stats2Chart;
    private Map<CandidateGeneListDetailPresenter.STATS, DataTable> stats2DataTable = Maps.newHashMap();
    private HandlerRegistration clickhandlerRegistration;

    private Modal editPopup = new Modal(true);
    private Modal deletePopup = new Modal(true);
    private Modal permissionPopUp = new Modal(true);
    private Modal resetEnrichmentPopup = new Modal(true);

    private static enum ACTION {UPLOAD, ADD, REMOVE}

    private BiMap<ACTION, ClickHandler> action2ClickHandler;
    private int enrichmentCount = 0;
    private GeneProxy geneToDelete = null;
    private final com.github.gwtbootstrap.client.ui.Button continueGeneBtn = new com.github.gwtbootstrap.client.ui.Button("Add");

    private final ClickHandler uploadClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            formPanel.submit();
        }
    };

    private final ClickHandler addGeneClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            getUiHandlers().onAddGene();
        }
    };

    private final ClickHandler removeClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            getUiHandlers().onDeleteGene(geneToDelete);
            geneToDelete = null;
        }
    };

    public static final ProvidesKey<GeneProxy> geneProvidesKey = new ProvidesKey<GeneProxy>() {
        @Override
        public Object getKey(GeneProxy item) {
            return item.getName();
        }
    };

    @Inject
    public CandidateGeneListDetailView(final Binder binder,
                                       final CandidateGeneListDisplayDriver candidateGeneListDisplayDriver,
                                       final CandidateGeneListView.CandidateGeneListEditDriver candidateGeneListEditDriver,
                                       final CustomDataGridResources dataGridResources,
                                       final CandidateGeneListDisplayEditor candidateGeneListDisplayEditor) {
        this.candidateGeneListDisplayDriver = candidateGeneListDisplayDriver;
        this.candidateGeneListDisplayEditor = candidateGeneListDisplayEditor;
        this.candidateGeneListEditDriver = candidateGeneListEditDriver;
        genesDataGrid = new DataGrid<GeneProxy>(50, dataGridResources, geneProvidesKey);
        searchGeneTa = new Typeahead(new SuggestOracle() {
            @Override
            public void requestSuggestions(Request request, Callback callback) {
                if (request.getQuery().length() >= minCharSize)
                    getUiHandlers().onSearchForGene(request, callback);
            }
        });
        widget = binder.createAndBindUi(this);

        stats2Chart = ImmutableBiMap.<CandidateGeneListDetailPresenter.STATS, PieChart>builder()
                .put(CandidateGeneListDetailPresenter.STATS.CHR, chrPieChart)
                .put(CandidateGeneListDetailPresenter.STATS.STRAND, strandPieChart)
                .put(CandidateGeneListDetailPresenter.STATS.ANNOTATION, annotationPieChart).build();
        this.candidateGeneListDisplayDriver.initialize(candidateGeneListDisplayEditor);
        this.candidateGeneListEditDriver.initialize(candidateGeneListEditEditor);
        initGeneDataGrid();
        genesPager.setDisplay(genesDataGrid);
        searchGeneTa.setUpdaterCallback(new Typeahead.UpdaterCallback() {
            @Override
            public String onSelection(SuggestOracle.Suggestion suggestion) {
                addGeneBtn.setEnabled(true);
                getUiHandlers().onSelectGene(suggestion);
                return suggestion.getReplacementString();
            }
        });

        permissionPopUp.setBackdrop(BackdropType.STATIC);
        permissionPopUp.setTitle("Permissions");
        permissionPopUp.setMaxHeigth("700px");
        permissionPopUp.setCloseVisible(false);
        permissionPopUp.setKeyboard(false);
        //initDataGrid();

        editPopup.setBackdrop(BackdropType.STATIC);
        editPopup.setCloseVisible(true);
        editPopup.setTitle("Edit study");


        resetEnrichmentPopup.setBackdrop(BackdropType.STATIC);
        resetEnrichmentPopup.setCloseVisible(false);
        resetEnrichmentPopup.setTitle("Add/remove gene");
        resetEnrichmentPopup.setKeyboard(false);


        com.github.gwtbootstrap.client.ui.Button cancelEnrichmentBtn = new com.github.gwtbootstrap.client.ui.Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                resetEnrichmentPopup.hide();
            }
        });
        cancelEnrichmentBtn.setType(ButtonType.DEFAULT);
        continueGeneBtn.setType(ButtonType.DANGER);
        resetEnrichmentPopup.add(new HTML("<h4>This list contains enrichment analyses.<br>If you modify the gene list, those analyes will be automatically removed.<br><br>Do you want to continue?</h4>"));
        action2ClickHandler = ImmutableBiMap.<ACTION, ClickHandler>builder()
                .put(ACTION.UPLOAD, uploadClickHandler)
                .put(ACTION.ADD, addGeneClickHandler)
                .put(ACTION.REMOVE, removeClickHandler).build();
        ModalFooter footer = new ModalFooter(cancelEnrichmentBtn, continueGeneBtn);
        resetEnrichmentPopup.add(footer);

        com.github.gwtbootstrap.client.ui.Button cancelEditBtn = new com.github.gwtbootstrap.client.ui.Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onCancel();
            }
        });
        cancelEditBtn.setType(ButtonType.DEFAULT);
        com.github.gwtbootstrap.client.ui.Button saveEditBtn = new com.github.gwtbootstrap.client.ui.Button("Save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onSave();
            }
        });
        saveEditBtn.setType(ButtonType.PRIMARY);
        footer = new ModalFooter(cancelEditBtn, saveEditBtn);
        editPopup.add(candidateGeneListEditEditor);
        editPopup.add(footer);

        deletePopup.setBackdrop(BackdropType.STATIC);
        deletePopup.setCloseVisible(true);
        deletePopup.add(new HTML("<h4>Do you really want to delete the study?</h4>"));
        com.github.gwtbootstrap.client.ui.Button cancelDeleteBtn = new com.github.gwtbootstrap.client.ui.Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deletePopup.hide();
            }
        });
        cancelDeleteBtn.setType(ButtonType.DEFAULT);
        com.github.gwtbootstrap.client.ui.Button deleteBtn = new com.github.gwtbootstrap.client.ui.Button("Delete", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onConfirmDelete();
            }
        });
        deleteBtn.setType(ButtonType.DANGER);
        deletePopup.add(new ModalFooter(cancelDeleteBtn, deleteBtn));
        shareBtn.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        fileUpload.setName("file");
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent event) {
                if (fileUpload.getText().equals("")) {
                    event.cancel();
                    ;
                }
            }
        });
        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                // Todo read result and display message if error
                formPanel.reset();
                getUiHandlers().refresh();
            }
        });
        actionBarPanel.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
    }

    @Override
    public void setUploadActionUrl(String url) {
        formPanel.setAction(url);
    }

    private void initGeneDataGrid() {
        genesDataGrid.setEmptyTableWidget(new HTML("<div style=\"font-weight:600;\">No genes in list.<br>Search and add genes</div>"));
        genesDataGrid.addColumn(new Column<GeneProxy, String>(new TextCell()) {
            @Override
            public String getValue(GeneProxy object) {
                return object.getName();
            }
        }, "Name");
        genesDataGrid.addColumn(new Column<GeneProxy, String>(new TextCell()) {
            @Override
            public String getValue(GeneProxy object) {
                return object.getName();
            }
        }, "Symbol");
        genesDataGrid.addColumn(new Column<GeneProxy, String>(new TextCell()) {
            @Override
            public String getValue(GeneProxy object) {
                return object.getName();
            }
        }, "Synonym");
        genesDataGrid.addColumn(new Column<GeneProxy, String>(new TextCell()) {
            @Override
            public String getValue(GeneProxy object) {
                return object.getChr();
            }
        }, "Chr");
        genesDataGrid.addColumn(new Column<GeneProxy, String>(new TextCell()) {
            @Override
            public String getValue(GeneProxy object) {
                return object.getStart() + " - " + object.getEnd();
            }
        }, "Position");
        genesDataGrid.addColumn(new Column<GeneProxy, String>(new TextCell()) {
            @Override
            public String getValue(GeneProxy object) {
                return object.getAnnotation();
            }
        }, "Type");
        List<HasCell<GeneProxy, ?>> hasCells = Lists.newArrayList();
        hasCells.add(new ActionHasCell(new EntypoIconActionCell<GeneProxy>("e_icon-trash", new ActionCell.Delegate<GeneProxy>() {
            @Override
            public void execute(GeneProxy object) {
                if (enrichmentCount == 0) {
                    getUiHandlers().onDeleteGene(object);
                } else {
                    geneToDelete = object;
                    showEnrichmentPopup(ACTION.REMOVE);
                }
            }
        }, true)));

        genesDataGrid.addColumn(new IdentityColumn<GeneProxy>(new CompositeCell<GeneProxy>(hasCells)) {
            @Override
            public GeneProxy getValue(GeneProxy object) {
                return object;
            }
        }, "Actions");
        genesDataGrid.setColumnWidth(3, 50, Style.Unit.PX);
        genesDataGrid.setColumnWidth(4, 200, Style.Unit.PX);
        genesDataGrid.setColumnWidth(6, 80, Style.Unit.PX);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public HasData<GeneProxy> getGenesDisplay() {
        return genesDataGrid;
    }

    @Override
    public CandidateGeneListDisplayDriver getDisplayDriver() {
        return candidateGeneListDisplayDriver;
    }

    @Override
    public CandidateGeneListView.CandidateGeneListEditDriver getEditDriver() {
        return candidateGeneListEditDriver;
    }

    @Override
    public void showPermissionPanel(boolean show) {
        if (show)
            permissionPopUp.show();
        else
            permissionPopUp.hide();
    }


    @UiHandler("addGeneBtn")
    public void onClickAddGeneBtn(ClickEvent e) {
        if (enrichmentCount == 0) {
            getUiHandlers().onAddGene();
        } else {
            showEnrichmentPopup(ACTION.ADD);
        }
    }

    @UiHandler("editBtn")
    public void onEdit(ClickEvent e) {
        getUiHandlers().onEdit();
    }

    @UiHandler("deleteBtn")
    public void onDelete(ClickEvent e) {
        getUiHandlers().onDelete();
    }

    @Override
    public void showEditPopup(boolean show) {
        if (show)
            editPopup.show();
        else
            editPopup.hide();
    }

    @UiHandler("shareBtn")
    public void onClickShareBtn(ClickEvent e) {
        getUiHandlers().onShare();
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == CandidateGeneListDetailPresenter.TYPE_SetPermissionContent) {
            permissionPopUp.add(content);
        } else if (slot == CandidateGeneListDetailPresenter.TYPE_SetEnrichmentCntent) {
            enrichmentContainer.add(content);
        } else if (slot == FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget) {
            facetContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void showDeletePopup(boolean show) {
        if (show)
            deletePopup.show();
        else
            deletePopup.hide();
    }

    @Override
    public void showShareBtn(boolean show) {
        shareBtn.setVisible(show);
    }

    @Override
    public void setShareTooltip(String toopltipMsg, IconType icon) {
        shareTooltip.setText(toopltipMsg);
        shareBtn.setIcon(icon);
        shareTooltip.reconfigure();
    }

    @Override
    public void showActionBtns(boolean show) {
        editBtn.setVisible(show);
        deleteBtn.setVisible(show);
        addGeneBtn.setVisible(show);
        uploadBtn.setVisible(show);
    }

    @Override
    public void phaseInPublication(GeneProxy gene) {
        (new PhaseAnimation.DataGridPhaseAnimation<GeneProxy>(genesDataGrid, gene, geneProvidesKey)).run();
    }

    @Override
    public HasText getSearchBox() {
        return searchGeneTb;
    }

    @Override
    public void enableAddBtn(boolean enable) {
        addGeneBtn.setEnabled(enable);
    }

    private DataTable createEmptyDataTable(String term) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, "stats");
        dataTable.addColumn(ColumnType.NUMBER, "count");
        dataTable.addRows(1);
        dataTable.setValue(0, 0, term);
        dataTable.setValue(0, 1, 1);
        return dataTable;
    }

    private PieChartOptions getPieOptions(String title, boolean isBlank) {
        PieChartOptions options = PieChartOptions.create();
        options.setTitle(title);
        if (isBlank) {
            options.setColors("#eee");
        }
        Animation animation = Animation.create();
        Options animationOptions = Options.create();
        return options;
    }

    private void drawCharts() {
        for (Map.Entry<CandidateGeneListDetailPresenter.STATS, PieChart> entry : stats2Chart.entrySet()) {
            if (stats2DataTable.containsKey(entry.getKey())) {
                PieChart chart = entry.getValue();
                entry.getValue().draw(stats2DataTable.get(entry.getKey()), getPieOptions(getTitleFromStat(entry.getKey()), false));
            }
        }
    }

    private String getTitleFromStat(CandidateGeneListDetailPresenter.STATS stat) {
        String title = "";
        switch (stat) {
            case CHR:
                title = "Chromosomes";
                break;
            case STRAND:
                title = "Strand";
                break;
            case ANNOTATION:
                title = "Annotation";
                break;
        }
        return title;
    }


    @Override
    public void setStatsData(DataTable dataTable, CandidateGeneListDetailPresenter.STATS stat) {
        stats2DataTable.put(stat, dataTable);
    }

    @Override
    public void refreshStats() {
        drawCharts();
    }

    @UiHandler("uploadBtn")
    public void onClickUploadBtn(ClickEvent e) {
        fileUpload.getElement().<InputElement>cast().click();
    }


    @UiHandler("fileUpload")
    public void onHandleFileSelect(ChangeEvent e) {
        if (enrichmentCount == 0) {
            formPanel.submit();
        } else {
            showEnrichmentPopup(ACTION.UPLOAD);
        }
    }


    private void showEnrichmentPopup(ACTION action) {
        ClickHandler handler = action2ClickHandler.get(action);
        if (clickhandlerRegistration != null) {
            clickhandlerRegistration.removeHandler();
        }
        clickhandlerRegistration = continueGeneBtn.addClickHandler(handler);
        String title = "";
        String btnText = "";
        switch (action) {
            case ADD:
                title = "Add gene";
                btnText = "Add";
                break;
            case UPLOAD:
                title = "Upload gene list";
                btnText = "Upload";
                break;
            case REMOVE:
                title = "Remove gene";
                btnText = "Remove";
                break;
        }
        resetEnrichmentPopup.setTitle(title);
        continueGeneBtn.setText(btnText);
        continueGeneBtn.setCompleteText(btnText);
        resetEnrichmentPopup.show();
    }

    @Override
    public void setEnrichmentCount(int count) {
        enrichmentCount = count;
        resetEnrichmentPopup.hide();
    }

}