package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Tooltip;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.editors.ExperimentDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.ExperimentEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter;
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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.PieChart;
import com.googlecode.gwt.charts.client.corechart.PieChartOptions;
import com.googlecode.gwt.charts.client.options.ChartArea;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendAlignment;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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
    Button edit;
    @UiField
    Button delete;
    @UiField
    com.github.gwtbootstrap.client.ui.Button share;
    @UiField(provided = true)
    ResponsiveDataGrid publicationDataGrid;
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
    ButtonGroup ontologyTypeBtnGrp;
    @UiField
    HTMLPanel actionBarPanel;
    @UiField
    NavLink navLinkIsaTab;
    private final ExperimentEditDriver experimentEditDriver;
    private final ExperimentDisplayDriver experimentDisplayDriver;
    private final MainResources resources;
    private Modal permissionPopUp = new Modal(true);
    private boolean layoutScheduled = false;
    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };
    private Modal editPopup = new Modal(true);
    private Modal deletePopup = new Modal(true);

    private ONTOLOGY_TYPE currentOntologyType = ConstEnums.ONTOLOGY_TYPE.TRAIT;
    private Map<ONTOLOGY_TYPE, DataTable> ontology2Map = new HashMap<ONTOLOGY_TYPE, DataTable>();

    public static class ResponsiveDataGrid extends DataGrid<PublicationProxy> {

        private final Column<PublicationProxy, String> citationColumn;
        private final Column<PublicationProxy, String> authorColumn;
        private final Column<PublicationProxy, String> titleColumn;
        private final Column<PublicationProxy, String> yearColumn;
        private final Column<PublicationProxy, String> journalColumn;
        private final Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam> doiColumn;
        private final IdentityColumn<PublicationProxy> actionColumn;
        private Boolean isCompact;
        private boolean showAction = true;


        public ResponsiveDataGrid(int pageSize, Resources resources, ActionCell.Delegate<PublicationProxy> actionDelegate) {
            super(pageSize, resources, new EntityProxyKeyProvider<PublicationProxy>());
            citationColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(object.getFirstAuthor() + " (" + object.getPubDate().getYear() + "). ");
                    builder.append(object.getTitle() + ". ");
                    builder.append(object.getJournal() + ", ");
                    builder.append(object.getVolume());
                    builder.append("(" + object.getIssue() + "), ");
                    builder.append(object.getPage() + ". ");
                    builder.append("doi:" + object.getDOI());
                    return builder.toString();
                }
            };
            authorColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return object.getFirstAuthor();
                }
            };
            titleColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return object.getTitle();
                }
            };
            yearColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return String.valueOf(object.getPubDate().getYear());
                }
            };
            journalColumn = new Column<PublicationProxy, String>(new TextCell()) {
                @Override
                public String getValue(PublicationProxy object) {
                    return object.getJournal();
                }
            };
            doiColumn = new Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam>(new HyperlinkCell()) {
                @Override
                public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(PublicationProxy object) {
                    return new HyperlinkPlaceManagerColumn.HyperlinkParam(object.getDOI(), object.getURL());
                }
            };
            actionColumn = new IdentityColumn<PublicationProxy>(new EntypoIconActionCell<PublicationProxy>("e_icon-trash", actionDelegate)) {

                @Override
                public PublicationProxy getValue(PublicationProxy object) {
                    return object;
                }
            };
            //initColumns();
            // updateColumns();
        }


        private void initColumns() {
            addColumn(citationColumn, "Citation");
            addColumn(authorColumn, "Author");
            addColumn(titleColumn, "Title");
            addColumn(yearColumn, "Year");
            addColumn(journalColumn, "Journal");
            addColumn(doiColumn, "DOI");
            addColumn(actionColumn);

        }

        private void updateColumns() {
            int columnCount = getColumnCount();
            for (int i = columnCount - 1; i >= 0; i--) {
                removeColumn(i);
            }
            removeUnusedColGroups();
            if (isCompact) {
                addColumn(citationColumn, "Citation");
                clearColumnWidth(0);
                if (showAction) {
                    addColumn(actionColumn);
                    setColumnWidth(1, "40px");
                }

            } else {
                addColumn(authorColumn, "Author");
                addColumn(titleColumn, "Title");
                addColumn(yearColumn, "Year");
                addColumn(journalColumn, "Journal");
                addColumn(doiColumn, "DOI");
                setColumnWidth(0, "100px");
                clearColumnWidth(1);
                setColumnWidth(2, "60px");
                setColumnWidth(3, "80px");
                clearColumnWidth(4);
                if (showAction) {
                    addColumn(actionColumn);
                    setColumnWidth(5, "40px");
                }
            }
        }

        @Override
        protected int getRealColumnCount() {
            return getColumnCount();
        }

        private void removeUnusedColGroups() {
            int columnCount = getColumnCount();
            NodeList<Element> colGroups = getElement().getElementsByTagName("colgroup");

            for (int i = 0; i < colGroups.getLength(); i++) {
                Element colGroupEle = colGroups.getItem(i);
                NodeList<Element> colList = colGroupEle.getElementsByTagName("col");

                for (int j = colList.getLength() - 1; j >= 0; j--) {
                    colGroupEle.removeChild(colList.getItem(j));
                }
            }
        }

        public void setShowAction(boolean showAction) {
            if (showAction != this.showAction) {
                this.showAction = showAction;
                updateColumns();
            }
        }


        @Override
        public void onResize() {
            super.onResize();
            if (!isAttached()) {
                return;
            }
            boolean isNewCompact = (getOffsetWidth() < 800);
            if (isCompact == null || isNewCompact != isCompact) {
                isCompact = isNewCompact;
                updateColumns();
            }
        }


    }

    @Inject
    public ExperimentDetailView(final Binder binder,
                                final ExperimentEditDriver experimentEditDriver,
                                final ExperimentDisplayDriver experimentDisplayDriver,
                                final MainResources resources,
                                final CustomDataGridResources customDataGridResources,
                                final ExperimentDisplayEditor experimentDisplayEditor) {
        this.resources = resources;
        this.experimentDisplayEditor = experimentDisplayEditor;
        publicationDataGrid = new ResponsiveDataGrid(50, customDataGridResources, new ActionCell.Delegate<PublicationProxy>() {
            @Override
            public void execute(PublicationProxy object) {
                getUiHandlers().onDeletePublication(object);
            }
        });
        widget = binder.createAndBindUi(this);
        this.experimentEditDriver = experimentEditDriver;
        this.experimentDisplayDriver = experimentDisplayDriver;
        this.experimentDisplayDriver.initialize(experimentDisplayEditor);
        this.experimentEditDriver.initialize(experimentEditEditor);
        permissionPopUp.setBackdrop(BackdropType.STATIC);
        permissionPopUp.setTitle("Permissions");
        permissionPopUp.setMaxHeigth("700px");
        permissionPopUp.setCloseVisible(false);
        permissionPopUp.setKeyboard(false);
        //initDataGrid();

        editPopup.setBackdrop(BackdropType.STATIC);
        editPopup.setCloseVisible(true);
        editPopup.setTitle("Edit study");
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
        ModalFooter footer = new ModalFooter(cancelEditBtn, saveEditBtn);
        editPopup.add(experimentEditEditor);
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
        actionBarPanel.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
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
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == ExperimentDetailPresenter.TYPE_SetPermissionContent) {
            permissionPopUp.add(content);
        } else {
            super.setInSlot(slot, content);
        }
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

    @UiHandler("share")
    public void onClickShareBtn(ClickEvent e) {
        getUiHandlers().onShare();
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

    @UiHandler("edit")
    public void onEdit(ClickEvent e) {
        getUiHandlers().onEdit();
    }

    @UiHandler("delete")
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

    @Override
    public void showDeletePopup(boolean show) {
        if (show)
            deletePopup.show();
        else
            deletePopup.hide();
    }

    @Override
    public void showShareBtn(boolean show) {
        share.setVisible(show);
    }

    @Override
    public void setShareTooltip(String toopltipMsg, IconType icon) {
        shareTooltip.setText(toopltipMsg);
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
