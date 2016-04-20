package com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes;

import com.github.timeu.gwtlibs.geneviewer.client.GeneViewer;
import com.github.timeu.gwtlibs.gwasviewer.client.events.GeneDataSource;
import com.gmi.nordborglab.browser.client.mvp.diversity.meta.topsnps.MetaSNPAnalysisDataGridColumns;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.ShowMoreCell;
import com.gmi.nordborglab.browser.client.util.TypeaheadUtils;
import com.gmi.nordborglab.browser.shared.proxy.AssociationProxy;
import com.gmi.nordborglab.browser.shared.proxy.MetaAnalysisProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Suggestion;
import org.gwtbootstrap3.extras.typeahead.client.base.SuggestionCallback;
import org.gwtbootstrap3.extras.typeahead.client.events.TypeaheadSelectedEvent;
import org.gwtbootstrap3.extras.typeahead.client.events.TypeaheadSelectedHandler;
import org.gwtbootstrap3.extras.typeahead.client.ui.Typeahead;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisGeneView extends ViewWithUiHandlers<MetaAnalysisGeneUiHandlers> implements
        MetaAnalysisGenePresenter.MyView {


    public static class ScoreCell extends AbstractCell<AssociationProxy> {

        private static NumberFormat format = NumberFormat.getDecimalFormat().overrideFractionDigits(2);


        interface Templates extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
            SafeHtml cell(SafeStyles styles, SafeHtml value);

        }

        public static Templates templates = GWT.create(Templates.class);


        @Override
        public void render(Context context, AssociationProxy value, SafeHtmlBuilder sb) {
            if (value == null)
                return;
            SafeStyles style = SafeStylesUtils.fromTrustedString("");
            if (value.isOverFDR()) {
                style = SafeStylesUtils.forTrustedColor("green");
            }
            sb.append(templates.cell(style, SafeHtmlUtils.fromString(format.format(value.getPValue()))));
        }

    }


    private int minCharSize = 3;

    interface Binder extends UiBinder<Widget, MetaAnalysisGeneView> {
    }

    private final Widget widget;

    @UiField
    GeneViewer geneViewer;
    @UiField(provided = true)
    DataGrid<MetaAnalysisProxy> flatDataGrid;
    @UiField
    CustomPager flatDataGridPager;
    @UiField(provided = true)
    DataGrid<MetaAnalysisProxy> groupedDataGrid;
    @UiField
    CustomPager groupedDataGridPager;
    @UiField
    LayoutPanel tableContainer;
    @UiField
    TextBox lowerLimitTb;
    @UiField
    SpanElement lowerLimitLb;
    @UiField
    TextBox upperLimitTb;
    @UiField
    SpanElement geneLb;
    @UiField
    SpanElement upperLimitLb;
    @UiField
    HTMLPanel searchForGeneLb;
    @UiField
    LayoutPanel contentPanel;
    @UiField(provided = true)
    org.gwtbootstrap3.extras.typeahead.client.ui.Typeahead searchGeneTa;
    @UiField
    SimplePanel filterContainer;
    @UiField
    LayoutPanel container;
    @UiField
    HTMLPanel searchContainer;
    @UiField
    HTMLPanel visualizationSelectionPanel;
    @UiField
    Button groupedBtn;
    @UiField
    Button flatBtn;
    @UiField
    Button heatmapBtn;
    @UiField
    TabLayoutPanel visualizationTabPanel;
    private final CustomDataGridResources customDataGridResources;
    private String chr = null;

    private MetaAnalysisGeneTableBuilder tableBuilder;

    private final PlaceManager placeManger;

    private Timer rangeChangeTimer = new Timer() {
        @Override
        public void run() {
            getUiHandlers().onChangeRange(-1 * Integer.parseInt(lowerLimitTb.getValue()), Integer.parseInt(upperLimitTb.getValue()));
        }
    };

    private final SelectionModel<MetaAnalysisProxy> groupedSelectionModel = new SingleSelectionModel<>();
    private final SelectionModel<MetaAnalysisProxy> flatSelectionModel = new SingleSelectionModel<>();
    private final Set<Long> showMore = new HashSet<>();


    @Inject
    public MetaAnalysisGeneView(Binder binder, GeneDataSource dataSource,
                                final PlaceManager placeManger,
                                final CustomDataGridResources customDataGridResources) {
        this.placeManger = placeManger;
        this.customDataGridResources = customDataGridResources;
        searchGeneTa = new Typeahead(new Dataset<SearchItemProxy>() {
            @Override
            public void findMatches(String query, SuggestionCallback<SearchItemProxy> suggestionCallback) {
                if (query.length() >= minCharSize)
                    getUiHandlers().onSearchForGene(query, new TypeaheadUtils(suggestionCallback));

            }
        });
        flatSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().onSelectMetaAnalysis(((SingleSelectionModel<MetaAnalysisProxy>) flatSelectionModel).getSelectedObject());
            }
        });
        flatDataGrid = createDatagrid(flatSelectionModel);
        initFlatDataTable();

        groupedSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().onSelectMetaAnalysis(((SingleSelectionModel<MetaAnalysisProxy>) groupedSelectionModel).getSelectedObject());
            }
        });
        groupedDataGrid = createDatagrid(groupedSelectionModel);
        initGroupedDataAble();
        widget = binder.createAndBindUi(this);
        bindSlot(MetaAnalysisGenePresenter.SLOT_FILTER_CONTENT, filterContainer);
        lowerLimitTb.getElement().setAttribute("type", "range");
        lowerLimitTb.getElement().setAttribute("min", "-20");
        lowerLimitTb.getElement().setAttribute("max", "0");
        lowerLimitTb.setValue("-10");
        upperLimitTb.setValue("10");
        upperLimitTb.getElement().setAttribute("min", "0");
        upperLimitTb.getElement().setAttribute("max", "20");
        upperLimitTb.getElement().setAttribute("type", "range");
        lowerLimitLb.setInnerText("-10 kb");
        upperLimitLb.setInnerText("+10 kb");
        geneLb.setInnerText("----------------");
        //TODO workaround for showing scrollbar when min-width is not met
        tableContainer.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.AUTO);
        visualizationSelectionPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        container.getWidgetContainerElement(searchContainer).getStyle().setOverflow(Style.Overflow.VISIBLE);

        flatDataGridPager.setDisplay(flatDataGrid);
        groupedDataGridPager.setDisplay(groupedDataGrid);
        searchGeneTa.addTypeaheadSelectedHandler(new TypeaheadSelectedHandler<SearchItemProxy>() {
            @Override
            public void onSelected(TypeaheadSelectedEvent<SearchItemProxy> typeaheadSelectedEvent) {
                Suggestion<SearchItemProxy> suggestion = typeaheadSelectedEvent.getSuggestion();
                getUiHandlers().onSelectGene(suggestion.getData().getReplacementText());
            }
        });
        try {
            geneViewer.load(() -> {
                // TODO make more robust so that we can add handlers before it is loaded
                geneViewer.addFetchGeneHandler(event -> {
                    if (chr == null)
                        return;
                    dataSource.fetchGenes(chr, event.getStart(), event.getEnd(), true, genes -> geneViewer.setGeneData(genes));
                });
                geneViewer.addHighlightGeneHandler(event -> {
                    dataSource.fetchGeneInfo(event.getGene().name, info -> geneViewer.setGeneInfo(info));
                });
            });
        } catch (Exception ex) {
            GWT.log(ex.getMessage());
        }
        flatDataGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<MetaAnalysisProxy>() {

            @Override
            public void onCellPreview(
                    CellPreviewEvent<MetaAnalysisProxy> event) {
                if ("mouseover".equals(event.getNativeEvent().getType())) {
                    MetaAnalysisProxy object = event.getValue();
                    getUiHandlers().onSelectMetaAnalysis(object);
            }
            }
        });

        groupedDataGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<MetaAnalysisProxy>() {

            @Override
            public void onCellPreview(
                    CellPreviewEvent<MetaAnalysisProxy> event) {
                if ("mouseover".equals(event.getNativeEvent().getType())) {
                    MetaAnalysisProxy object = event.getValue();

                    if (event.getContext().getSubIndex() > 0) {
                        int assocIndex = getAssocIndexFromEvent(Element.as(event.getNativeEvent().getEventTarget()));
                        if (assocIndex >= 0 && assocIndex < object.getAssociations().size()) {
                            getUiHandlers().onSelectAssociation(object.getAssociations().get(assocIndex));
                        }
                    } else {
                        getUiHandlers().onSelectMetaAnalysis(object);
                    }
                }
            }
        });
        changeView(MetaAnalysisGenePresenter.VIZ_TYPE.GROUPED);
    }


    private int getAssocIndexFromEvent(Element element) {
        GQuery parentElement = GQuery.$(element).parent("tr[__assoc_ix]");
        if (parentElement.isEmpty())
            return -1;
        return Integer.valueOf(parentElement.attr("__assoc_ix"));
    }


    @UiHandler("lowerLimitTb")
    public void onLowerLimitTbChanged(ChangeEvent e) {
        lowerLimitLb.setInnerText(lowerLimitTb.getValue() + " kb");
        rangeChangeTimer.cancel();
        rangeChangeTimer.schedule(1000);
    }

    @UiHandler("upperLimitTb")
    public void onUpperLimitTbChanged(ChangeEvent e) {
        upperLimitLb.setInnerText("+ " + upperLimitTb.getValue() + " kb");
        rangeChangeTimer.cancel();
        rangeChangeTimer.schedule(1000);
    }

    @Override
    public HasData<MetaAnalysisProxy> getFlatDisplay() {
        return flatDataGrid;
    }

    @Override
    public HasData<MetaAnalysisProxy> getGroupedDisplay() {
        return groupedDataGrid;
    }

    @Override
    public void setGeneViewerSelection(long position) {
        geneViewer.setSelectionLine((int) position);
    }

    @Override
    public void reset() {
        geneLb.setInnerText("----------------");
        contentPanel.setWidgetVisible(searchForGeneLb, true);
        chr = null;
        geneViewer.setChromosome(null);
        geneViewer.setViewRegion(0, 1);
        geneViewer.updateZoom(0, 1);
        searchGeneTa.setValue("");
    }

    @Override
    public void setPagingDisabled(boolean disabled) {
        flatDataGridPager.setPageControlDisabled(disabled);
    }

    @Override
    public void setActiveVisualization(MetaAnalysisGenePresenter.VIZ_TYPE vizType) {
        groupedBtn.setActive(false);
        flatBtn.setActive(false);
        heatmapBtn.setActive(false);
        switch (vizType) {
            case GROUPED:
                groupedBtn.setActive(true);
                break;
            case FLAT:
                flatBtn.setActive(true);
                break;
            case HEATMAP:
                heatmapBtn.setActive(true);
                break;
        }
        // TODO change actual visualization
        changeView(vizType);
    }

    private DataGrid<MetaAnalysisProxy> createDatagrid(SelectionModel<MetaAnalysisProxy> selectionModel) {
        DataGrid<MetaAnalysisProxy> dataGrid = new DataGrid<>(25, customDataGridResources);
        dataGrid.setSelectionModel(selectionModel);
        dataGrid.setWidth("100%");
        dataGrid.setMinimumTableWidth(1000, Style.Unit.PX);
        dataGrid.setEmptyTableWidget(new Label("No Records found"));
        return dataGrid;
    }

    private void changeView(MetaAnalysisGenePresenter.VIZ_TYPE vizType) {
        int index = 0;
        switch (vizType) {
            case GROUPED:
                index = 0;
                break;
            case FLAT:
                index = 1;
                break;
            case HEATMAP:
                index = 2;
        }
        visualizationTabPanel.selectTab(index);
    }

    private void initGroupedDataAble() {
        Column<MetaAnalysisProxy, Boolean> showMoreColumn = new Column<MetaAnalysisProxy, Boolean>(new ShowMoreCell()) {
            @Override
            public Boolean getValue(MetaAnalysisProxy object) {
                return showMore.contains(object.getAnalysisId());
            }
        };
        showMoreColumn.setFieldUpdater(new FieldUpdater<MetaAnalysisProxy, Boolean>() {
            @Override
            public void update(int index, MetaAnalysisProxy object, Boolean value) {
                if (showMore.contains(object.getAnalysisId())) {
                    showMore.remove(object.getAnalysisId());
                } else {
                    showMore.add(object.getAnalysisId());
                }
                // Redraw the modified row.
                groupedDataGrid.redrawRow(index);
            }
        });
        groupedDataGrid.setColumnWidth(0, 30, Style.Unit.PX);
        groupedDataGrid.addColumn(showMoreColumn);
        groupedDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.AnalysisColumn(placeManger), "Analysis");
        groupedDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.PhenotypeColumn(placeManger), "Phenotype");
        groupedDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.StudyColumn(placeManger), "Study");
        groupedDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.GenotypeColumn(), "Genotype");
        groupedDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MethodColumn(), "Method");
        groupedDataGrid.setColumnWidth(5, 80, Style.Unit.PX);
        groupedDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.AssocCountColumn(), "# Assoc.");
        groupedDataGrid.setColumnWidth(6, 80, Style.Unit.PX);
        tableBuilder = new MetaAnalysisGeneTableBuilder(groupedDataGrid, showMore, 0);
        groupedDataGrid.setTableBuilder(tableBuilder);

    }

    private void initFlatDataTable() {
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.AnalysisColumn(placeManger), "Analysis");
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.PhenotypeColumn(placeManger), "Phenotype");
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.StudyColumn(placeManger), "Study");
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.GenotypeColumn(), "Genotype");
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MethodColumn(), "Method");
        flatDataGrid.setColumnWidth(4, 80, Style.Unit.PX);
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.ScoreColumn(), "pVal");
        flatDataGrid.setColumnWidth(5, 60, Style.Unit.PX);
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MafColumn(), "Maf");
        flatDataGrid.setColumnWidth(6, 60, Style.Unit.PX);
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MacColumn(), "Mac");
        flatDataGrid.setColumnWidth(7, 60, Style.Unit.PX);
        flatDataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.SNPColumn(), "SNP");
        flatDataGrid.setColumnWidth(8, 100, Style.Unit.PX);
    }




    @Override
    public void setGeneViewerRegion(String chr, int start, int end, int totalLength) {
        this.chr = "Chr" + chr;
        geneViewer.setChromosome("Chr" + chr);
        geneViewer.setViewRegion(0, totalLength);
        geneViewer.updateZoom(start, end);
        contentPanel.setWidgetVisible(searchForGeneLb, false);
    }

    @Override
    public void setGene(String gene) {
        geneLb.setInnerText(gene);
    }

    @Override
    public void setGeneRange(Integer leftInterval, Integer rightInterval) {
        upperLimitTb.setValue(rightInterval.toString());
        upperLimitLb.setInnerText("+ " + rightInterval.toString() + " kb");

        lowerLimitTb.setValue("-" + leftInterval);
        lowerLimitLb.setInnerText("- " + leftInterval.toString() + " kb");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @UiHandler("groupedBtn")
    public void onClickGroupedBtn(ClickEvent e) {
        getUiHandlers().onSelectVisualization(MetaAnalysisGenePresenter.VIZ_TYPE.GROUPED);
    }

    @UiHandler("flatBtn")
    public void onClickFlatBtn(ClickEvent e) {
        getUiHandlers().onSelectVisualization(MetaAnalysisGenePresenter.VIZ_TYPE.FLAT);
    }

    @UiHandler("heatmapBtn")
    public void onClickHeatmapBtn(ClickEvent e) {
        getUiHandlers().onSelectVisualization(MetaAnalysisGenePresenter.VIZ_TYPE.HEATMAP);
    }

    @Override
    public void setMaxAssocCount(int maxAssocCount) {
        tableBuilder.setMaxAssocCount(maxAssocCount);
    }
}