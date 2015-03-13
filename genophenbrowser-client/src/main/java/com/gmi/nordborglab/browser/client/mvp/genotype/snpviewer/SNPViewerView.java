package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class SNPViewerView extends ViewWithUiHandlers<SNPViewerUiHandlers> implements SNPViewerPresenter.MyView {

    interface Binder extends UiBinder<Widget, SNPViewerView> {
    }

    public interface MyStyle extends CssResource {
        String loadingIndicator();
    }

    @UiField(provided = true)
    ValueListBox<AlleleAssayProxy> genotypeLb;
    @UiField
    TextBox phenotypeTb;
    @UiField(provided = true)
    Typeahead phenotypeTa;
    @UiField(provided = true)
    DataGrid<SNPInfoProxy> snpsDataGrid;
    @UiField
    CustomPager snpsPager;
    @UiField
    LayoutPanel snpDataGridContainer;
    @UiField
    DeckLayoutPanel snpDetailContainerDeckPanel;
    @UiField
    ResizeLayoutPanel snpContainer;
    @UiField
    DisclosurePanel filterContainer;
    @UiField
    HTMLPanel container;

    @UiField
    MyStyle style;
    @UiField
    TextBox regionTb;
    @UiField
    SimpleLayoutPanel snpDetailPresenterContainer;
    @UiField
    LayoutPanel snpDetailContainer;

    private boolean layoutScheduled = false;
    private Label snpsDataGridEmptyWidget = new Label("No Records found");
    private Label snpsDataLoadingIndicator = new Label("Set the filter above to load data...");
    private final Widget defaultDataGridLoadingIndicator;
    private SingleSelectionModel<SNPInfoProxy> selectionModel = new SingleSelectionModel<>();

    private int minCharSize = 3;

    private Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
            layoutScheduled = false;
            int height = asWidget().getOffsetHeight() - filterContainer.getOffsetHeight();
            if (height > 0) {
                snpContainer.setHeight(height + "px");
            }
        }
    };

    @Inject
    public SNPViewerView(Binder binder, final CustomDataGridResources customDataGridResources) {
        genotypeLb = new ValueListBox<>(new ProxyRenderer<AlleleAssayProxy>(null) {
            @Override
            public String render(AlleleAssayProxy object) {
                if (object == null) {
                    return "Select a genotype";
                }
                return object.getName() + " (" + object.getAvailableAllelesCount() + ")";
            }
        });
        phenotypeTa = new Typeahead(new SuggestOracle() {
            @Override
            public void requestSuggestions(Request request, Callback callback) {
                if (request.getQuery().length() >= minCharSize)
                    getUiHandlers().onSearchPhenotype(request, callback);
            }
        });
        snpsDataGrid = new DataGrid(50, customDataGridResources);
        defaultDataGridLoadingIndicator = snpsDataGrid.getLoadingIndicator();
        initWidget(binder.createAndBindUi(this));

        phenotypeTa.setUpdaterCallback(new Typeahead.UpdaterCallback() {
            @Override
            public String onSelection(SuggestOracle.Suggestion suggestion) {
                getUiHandlers().onSelectPhenotype(suggestion);
                return suggestion.getReplacementString();
            }
        });
        snpDetailContainerDeckPanel.showWidget(0);
        initSNPsDataGrid();
        snpsPager.setDisplay(snpsDataGrid);
        snpsDataLoadingIndicator.addStyleName(style.loadingIndicator());
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                getUiHandlers().onSelectSNP(selectionModel.getSelectedObject());
            }
        });
        snpsDataGrid.setSelectionModel(selectionModel);
    }

    private void initSNPsDataGrid() {
        snpsDataGrid.setWidth("100%");
        snpsDataGrid.setMinimumTableWidth(1000, Style.Unit.PX);
        snpsDataGrid.setEmptyTableWidget(snpsDataGridEmptyWidget);
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.InGeneColumn(), "Genic");
        snpsDataGrid.setColumnWidth(0, 50, Style.Unit.PX);
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.GeneColumn(), "Gene");
        snpsDataGrid.setColumnWidth(1, 120, Style.Unit.PX);
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.PositionColumn(), "Position");
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.EffectColumn(), "Effects");
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.FunctionColumn(), "Functions");
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.CodonColumn(), "Codon");
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.AminoAcidColumn(), "Amino");
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.LyrColumn(), "Lyr");
        snpsDataGrid.setColumnWidth(7, 80, Style.Unit.PX);
        List<HasCell<SNPInfoProxy, ?>> cells = Lists.newArrayList();
        cells.add(new SNPViewerDataGridColumns.AlleleTypeCell(false));
        cells.add(new SNPViewerDataGridColumns.AlleleCountCell(false));
        cells.add(new SNPViewerDataGridColumns.AlleleCountBarCell(false));
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.AlleleColumn(cells), "Ref");

        cells = Lists.newArrayList();
        cells.add(new SNPViewerDataGridColumns.AlleleTypeCell(true));
        cells.add(new SNPViewerDataGridColumns.AlleleCountCell(true));
        cells.add(new SNPViewerDataGridColumns.AlleleCountBarCell(true));
        snpsDataGrid.addColumn(new SNPViewerDataGridColumns.AlleleColumn(cells), "Alt");
    }

    @Override
    public void setAvailableGenotypes(List<AlleleAssayProxy> alleleAssayList) {
        genotypeLb.setAcceptableValues(alleleAssayList);
    }

    @Override
    public void setGenotype(AlleleAssayProxy genotype) {
        genotypeLb.setValue(genotype);
    }

    @Override
    public void setRegion(String region) {
        regionTb.setValue(region);
    }

    @Override
    public void showRegionError() {

    }

    @Override
    public void clearRegionError() {

    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == SNPViewerPresenter.TYPE_SetSNPDetailContent) {
            snpDetailPresenterContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }


    @Override
    public HasData<SNPInfoProxy> getSNPSDisplay() {
        return snpsDataGrid;
    }

    @Override
    public void setPhenotype(String phenotype) {
        phenotypeTb.setValue(phenotype);
    }

    @Override
    public void showDefaultLoadingIndicator(boolean show) {
        Widget loadingIndicator = defaultDataGridLoadingIndicator;
        if (!show)
            loadingIndicator = snpsDataLoadingIndicator;
        snpsDataGrid.setLoadingIndicator(loadingIndicator);
    }

    @Override
    public void showSNPDetail(boolean show) {
        snpDetailContainerDeckPanel.showWidget(show ? snpDetailContainer : snpDataGridContainer);
        if (!show) {
            selectionModel.clear();
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        resize();
    }

    private void resize() {
        if (asWidget().isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    @UiHandler("filterContainer")
    public void onOpenFilter(OpenEvent<DisclosurePanel> e) {
        resize();
    }

    @UiHandler("filterContainer")
    public void onCloseFilter(CloseEvent<DisclosurePanel> e) {
        resize();
    }


    @UiHandler("genotypeLb")
    public void onGenotypeChanged(ValueChangeEvent<AlleleAssayProxy> e) {
        getUiHandlers().onSelectAlleleAssay(e.getValue());
    }


    @UiHandler("regionTb")
    public void onKeyUpRegionTb(KeyUpEvent e) {
        if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || regionTb.getValue().equalsIgnoreCase("")) {
            getUiHandlers().onSelectRegion(regionTb.getValue());
        }
    }

    @UiHandler("phenotypeTb")
    public void onKeyUpPhenotypeTb(KeyUpEvent e) {
        if (phenotypeTb.getValue().equalsIgnoreCase("")) {
            getUiHandlers().onSelectPhenotype(null);
        }
    }

    @UiHandler("snpDetailBackBtn")
    public void onClickSNPDetailBack(ClickEvent e) {
        selectionModel.clear();
    }

}