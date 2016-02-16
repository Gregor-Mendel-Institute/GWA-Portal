package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail;

import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListEnrichmentProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.gwtbootstrap3.client.ui.Button;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.12.13
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
public class CandidateGeneListEnrichmentPresenterWidgetView extends ViewWithUiHandlers<CandidateGeneListEnrichmentUiHandlers> implements CandidateGeneListEnrichmentPresenterWidget.MyView {

    interface Binder extends UiBinder<Widget, CandidateGeneListEnrichmentPresenterWidgetView> {

    }

    private final Widget widget;
    private int minCharSize = 3;
    private final ConstEnums.ENRICHMENT_TYPE type;

    private final DataGrid<CandidateGeneListEnrichmentProxy> finishedDataGrid;
    private final CustomPager finishedPager = new CustomPager();
    private final DataGrid<CandidateGeneListEnrichmentProxy> runningDataGrid;
    private final CustomPager runningPager = new CustomPager();
    private final DataGrid<CandidateGeneListEnrichmentProxy> availableDataGrid;
    private final CustomPager availablePager = new CustomPager();
    private final ProvidesKey<CandidateGeneListEnrichmentProxy> keyProvider = new EntityProxyKeyProvider<CandidateGeneListEnrichmentProxy>();
    private final MultiSelectionModel<CandidateGeneListEnrichmentProxy> availableSelectionModel = new MultiSelectionModel<CandidateGeneListEnrichmentProxy>(keyProvider);
    private final CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxState checkBoxState = new CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxStateImpl();
    private final CandidateGeneListEnrichmentDataGridColumns.PValueBarHasCell pValueBarHasCell = new CandidateGeneListEnrichmentDataGridColumns.PValueBarHasCell();
    private final CandidateGeneListEnrichmentDataGridColumns.CheckBoxHeader checkBoxHeader = new CandidateGeneListEnrichmentDataGridColumns.CheckBoxHeader();
    private final CandidateGeneListEnrichmentDataGridColumns.CheckBoxFooter checkBoxFooter = new CandidateGeneListEnrichmentDataGridColumns.CheckBoxFooter(checkBoxState);

    @UiField
    SimpleLayoutPanel pagerContainer;
    @UiField
    SimpleLayoutPanel dataGridContainer;
    @UiField
    Button runBtn;
    @UiField
    SimplePanel facetContainer;

    private final PlaceManager placeManager;

    @Inject
    public CandidateGeneListEnrichmentPresenterWidgetView(final Binder binder, final CustomDataGridResources dataGridResources,
                                                          final PlaceManager placeManager, @Assisted ConstEnums.ENRICHMENT_TYPE type) {
        this.placeManager = placeManager;
        this.type = type;
        finishedDataGrid = new DataGrid<CandidateGeneListEnrichmentProxy>(50, dataGridResources, keyProvider);
        runningDataGrid = new DataGrid<CandidateGeneListEnrichmentProxy>(50, dataGridResources, keyProvider);
        availableDataGrid = new DataGrid<CandidateGeneListEnrichmentProxy>(50, dataGridResources, keyProvider);
        widget = binder.createAndBindUi(this);
        bindSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetContainer);
        initDataGrids();
        finishedPager.setDisplay(finishedDataGrid);
        runningPager.setDisplay(runningDataGrid);
        availablePager.setDisplay(availableDataGrid);
    }

    private void initDataGrids() {
        //TODO workaround. Later fix so that it also works for experiment/phenotype
        //navAvailable.setVisible((type == EnrichmentProvider.TYPE.CANDIDATE_GENE_LIST || type == EnrichmentProvider.TYPE.STUDY));
        initFinishedGrid();
        initRunningGrid();
        initAvailableGrid();
    }

    private void initAvailableGrid() {
        availableDataGrid.setSelectionModel(availableSelectionModel, DefaultSelectionEventManager
                .<CandidateGeneListEnrichmentProxy>createCheckboxManager());
        availableDataGrid.setWidth("100%");
        availableDataGrid.setEmptyTableWidget(new Label("No Records found"));
        checkBoxFooter.setUpdater(new ValueUpdater<CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxState>() {
            @Override
            public void update(CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxState value) {
                if (!value.showCheckAll()) {
                    checkBoxHeader.reverseChecked();
                }
                getUiHandlers().selectAllRecords(!value.showCheckAll());
            }
        });

        checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {

            @Override
            public void update(Boolean value) {
                checkBoxHeader.reverseChecked();
                getUiHandlers().selectVisibleRecords(value);
                checkBoxState.setShowCheckAll(value);
            }
        });
        availableDataGrid.addColumn(new Column<CandidateGeneListEnrichmentProxy, Boolean>(new CheckboxCell()) {
            @Override
            public Boolean getValue(CandidateGeneListEnrichmentProxy object) {
                return availableSelectionModel.isSelected(object);
            }
        }, checkBoxHeader, checkBoxFooter);
        availableDataGrid.setColumnWidth(0, 50, Style.Unit.PX);
        addCommonColumns(availableDataGrid, checkBoxFooter);

    }

    private void addCommonColumns(DataGrid<CandidateGeneListEnrichmentProxy> grid, CandidateGeneListEnrichmentDataGridColumns.CheckBoxFooter checkBoxFooter) {
        PlaceRequest.Builder studyRequest = new PlaceRequest.Builder().nameToken(NameTokens.study);
        PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.candidateGeneListDetail);
        if (type != ConstEnums.ENRICHMENT_TYPE.CANDIDATE_GENE_LIST) {
            grid.addColumn(new CandidateGeneListEnrichmentDataGridColumns.TitleColumn(placeManager, request), new TextHeader("List"), checkBoxFooter);
        }
        if (type != ConstEnums.ENRICHMENT_TYPE.ANALYSIS) {
            grid.addColumn(new CandidateGeneListEnrichmentDataGridColumns.StudyColumn(placeManager, studyRequest), new TextHeader("Analysis"), checkBoxFooter);

            if (type != ConstEnums.ENRICHMENT_TYPE.PHENOTYPE) {
                grid.addColumn(new Column<CandidateGeneListEnrichmentProxy, String>(new TextCell()) {
                    @Override
                    public String getValue(CandidateGeneListEnrichmentProxy object) {
                        return object.getStudy().getPhenotype().getLocalTraitName();
                    }
                }, new TextHeader("Phenotype"), checkBoxFooter);

                if (type != ConstEnums.ENRICHMENT_TYPE.STUDY) {
                    grid.addColumn(new Column<CandidateGeneListEnrichmentProxy, String>(new TextCell()) {
                        @Override
                        public String getValue(CandidateGeneListEnrichmentProxy object) {
                            return object.getStudy().getPhenotype().getExperiment().getName();
                        }
                    }, new TextHeader("Experiment"), checkBoxFooter);
                }
            }
        }
        grid.addColumn(new Column<CandidateGeneListEnrichmentProxy, String>(new TextCell()) {
            @Override
            public String getValue(CandidateGeneListEnrichmentProxy object) {
                return object.getStudy().getAlleleAssay().getName();
            }
        }, new TextHeader("Genotype"), checkBoxFooter);
        grid.addColumn(new Column<CandidateGeneListEnrichmentProxy, String>(new TextCell()) {
            @Override
            public String getValue(CandidateGeneListEnrichmentProxy object) {
                return object.getStudy().getTransformation().getName();
            }
        }, new TextHeader("Trans."), checkBoxFooter);

    }

    private void initRunningGrid() {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.study).build();
        runningDataGrid.setWidth("100%");
        runningDataGrid.setEmptyTableWidget(new Label("No running analyses"));
        addCommonColumns(runningDataGrid, null);
        List<HasCell<CandidateGeneListEnrichmentProxy, ?>> cells = Lists.newArrayList();
        cells.add(new CandidateGeneListEnrichmentDataGridColumns.StatusCell());
        cells.add(new CandidateGeneListEnrichmentDataGridColumns.ProgressCell());
        runningDataGrid.addColumn(new CandidateGeneListEnrichmentDataGridColumns.StatusColumn(cells), new TextHeader("Status"));
    }

    private void initFinishedGrid() {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.study).build();
        finishedDataGrid.setWidth("100%");
        finishedDataGrid.setEmptyTableWidget(new Label("No Records found"));
        addCommonColumns(finishedDataGrid, null);
        List<HasCell<CandidateGeneListEnrichmentProxy, ?>> cells = Lists.newArrayList();
        cells.add(pValueBarHasCell);
        cells.add(new CandidateGeneListEnrichmentDataGridColumns.PValueHasCell());
        finishedDataGrid.addColumn(new IdentityColumn<CandidateGeneListEnrichmentProxy>(new CandidateGeneListEnrichmentDataGridColumns.PValueCell(cells)), new TextHeader("Pvalue"));
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void displayType(ConstEnums.ENRICHMENT_FILTER type) {
        DataGrid grid = null;
        CustomPager pager = null;
        switch (type) {
            case FINISHED:
                grid = finishedDataGrid;
                pager = finishedPager;
                break;
            case RUNNING:
                grid = runningDataGrid;
                pager = runningPager;
                break;
            case AVAILABLE:
                grid = availableDataGrid;
                pager = availablePager;
                break;
        }
        if (grid != null && grid != dataGridContainer.getWidget()) {
            dataGridContainer.setWidget(grid);
            pagerContainer.setWidget(pager);
        }
    }


    @Override
    public HasData<CandidateGeneListEnrichmentProxy> getFinishedDisplay() {
        return finishedDataGrid;
    }

    @Override
    public HasData<CandidateGeneListEnrichmentProxy> getRunningDisplay() {
        return runningDataGrid;
    }

    @Override
    public HasData<CandidateGeneListEnrichmentProxy> getAvailableDisplay() {
        return availableDataGrid;
    }

    @Override
    public void enableRunBtn(boolean enabled) {
        runBtn.setEnabled(enabled);
    }

    @Override
    public CandidateGeneListEnrichmentDataGridColumns.MultiCheckBoxState getCheckBoxState() {
        return checkBoxState;
    }

    @Override
    public void redrawhHeader() {
        availableDataGrid.redrawHeaders();
    }

    @Override
    public void setMaxPvalue(double maxPvalue) {
        pValueBarHasCell.setMaxPvalue(maxPvalue);
    }

    @UiHandler("runBtn")
    public void onClickRunBtn(ClickEvent e) {
        getUiHandlers().onRunEnrichment();

    }
}