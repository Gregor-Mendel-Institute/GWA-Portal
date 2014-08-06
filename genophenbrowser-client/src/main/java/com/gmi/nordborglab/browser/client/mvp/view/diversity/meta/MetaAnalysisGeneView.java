package com.gmi.nordborglab.browser.client.mvp.view.diversity.meta;

import at.gmi.nordborglab.widgets.geneviewer.client.GeneViewer;
import at.gmi.nordborglab.widgets.geneviewer.client.datasource.DataSource;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.gmi.nordborglab.browser.client.mvp.handlers.MetaAnalysisGeneUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.MetaAnalysisGenePresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisGeneView extends ViewWithUiHandlers<MetaAnalysisGeneUiHandlers> implements
        MetaAnalysisGenePresenter.MyView {


    public static class ScoreCell extends AbstractCell<MetaSNPAnalysisProxy> {

        private static NumberFormat format = NumberFormat.getDecimalFormat().overrideFractionDigits(2);


        interface Templates extends SafeHtmlTemplates {

            @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
            SafeHtml cell(SafeStyles styles, SafeHtml value);

        }

        private static Templates templates = GWT.create(Templates.class);


        @Override
        public void render(Context context, MetaSNPAnalysisProxy value, SafeHtmlBuilder sb) {
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
    com.github.gwtbootstrap.client.ui.TextBox searchGeneTb;
    @UiField
    GeneViewer geneViewer;
    @UiField(provided = true)
    DataGrid<MetaSNPAnalysisProxy> dataGrid;
    @UiField
    CustomPager pager;
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
    Typeahead searchGeneTa;
    @UiField
    SimplePanel filterContainer;
    @UiField
    LayoutPanel container;
    @UiField
    HTMLPanel searchContainer;

    private final PlaceManager placeManger;

    private Timer rangeChangeTimer = new Timer() {
        @Override
        public void run() {
            getUiHandlers().onChangeRange(-1 * Integer.parseInt(lowerLimitTb.getValue()), Integer.parseInt(upperLimitTb.getValue()));
        }
    };


    private final SelectionModel<MetaSNPAnalysisProxy> selectionModel = new SingleSelectionModel<MetaSNPAnalysisProxy>();

    @Inject
    public MetaAnalysisGeneView(Binder binder, DataSource dataSource,
                                final PlaceManager placeManger,
                                final CustomDataGridResources customDataGridResources) {
        this.placeManger = placeManger;
        searchGeneTa = new Typeahead(new SuggestOracle() {
            @Override
            public void requestSuggestions(Request request, Callback callback) {
                if (request.getQuery().length() >= minCharSize)
                    getUiHandlers().onSearchForGene(request, callback);
            }
        });
        dataGrid = new DataGrid<MetaSNPAnalysisProxy>(20, customDataGridResources);
        dataGrid.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().onSelectMetaAnalysis(((SingleSelectionModel<MetaSNPAnalysisProxy>) selectionModel).getSelectedObject());
            }
        });
        initGrid();
        widget = binder.createAndBindUi(this);
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


        container.getWidgetContainerElement(searchContainer).getStyle().setOverflow(Style.Overflow.VISIBLE);

        pager.setDisplay(dataGrid);
        searchGeneTa.setUpdaterCallback(new Typeahead.UpdaterCallback() {
            @Override
            public String onSelection(SuggestOracle.Suggestion suggestion) {
                getUiHandlers().onSelectGene(suggestion);
                return suggestion.getReplacementString();
            }
        });
        geneViewer.setHeight("290px");
        geneViewer.setDataSource(dataSource);
        try {
            geneViewer.load(null);
        } catch (Exception ex) {

        }
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MetaAnalysisGenePresenter.TYPE_FilterContent) {
            filterContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
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

    private void initGrid() {
        dataGrid.setWidth("100%");
        dataGrid.setMinimumTableWidth(1000, Style.Unit.PX);
        dataGrid.setEmptyTableWidget(new Label("No Records found"));
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.AnalysisColumn(placeManger), "Analysis");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.PhenotypeColumn(placeManger), "Phenotype");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.StudyColumn(placeManger), "Study");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.GenotypeColumn(), "Genotype");
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MethodColumn(), "Method");
        dataGrid.setColumnWidth(4, 80, Style.Unit.PX);
        dataGrid.addColumn(new IdentityColumn<MetaSNPAnalysisProxy>(new MetaAnalysisGeneView.ScoreCell()), "pVal");
        dataGrid.setColumnWidth(5, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MafColumn(), "Maf");
        dataGrid.setColumnWidth(6, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.MacColumn(), "Mac");
        dataGrid.setColumnWidth(7, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.ChrColumn(), "Chr");
        dataGrid.setColumnWidth(8, 60, Style.Unit.PX);
        dataGrid.addColumn(new MetaSNPAnalysisDataGridColumns.SNPColumn(), "SNP");
        dataGrid.setColumnWidth(9, 100, Style.Unit.PX);
    }

    @Override
    public HasData<MetaSNPAnalysisProxy> getDisplay() {
        return dataGrid;
    }

    @Override
    public void setGeneViewerSelection(long position) {
        geneViewer.setSelectionLine((int) position);
    }

    @Override
    public void reset() {
        geneLb.setInnerText("----------------");
        contentPanel.setWidgetVisible(searchForGeneLb, true);
        geneViewer.setChromosome(null);
        geneViewer.setViewRegion(0, 1);
        geneViewer.updateZoom(0, 1);
        searchGeneTb.setValue("");
    }


    @Override
    public void setGeneViewerRegion(String chr, int start, int end, int totalLength) {
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
}