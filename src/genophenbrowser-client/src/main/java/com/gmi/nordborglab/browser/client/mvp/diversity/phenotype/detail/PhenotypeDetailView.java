package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail;

import com.gmi.nordborglab.browser.client.editors.PhenotypeDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.detail.StudyDetailPresenter.LOWER_CHART_TYPE;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.detail.StudyDetailPresenter.UPPER_CHART_TYPE;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitStatsProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.corechart.PieChart;
import com.googlecode.gwt.charts.client.corechart.PieChartOptions;
import com.googlecode.gwt.charts.client.geochart.GeoChart;
import com.googlecode.gwt.charts.client.geochart.GeoChartOptions;
import com.googlecode.gwt.charts.client.motionchart.MotionChart;
import com.googlecode.gwt.charts.client.motionchart.MotionChartOptions;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.Tooltip;
import com.googlecode.gwt.charts.client.options.TooltipTrigger;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.SimpleCallback;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PhenotypeDetailView extends ViewWithUiHandlers<PhenotypeDetailUiHandlers> implements
        PhenotypeDetailPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, PhenotypeDetailView> {
    }

    public interface PhenotypeDisplayDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeDisplayEditor> {
    }

    public interface PhenotypeEditDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeEditEditor> {
    }

    @UiField(provided = true)
    PhenotypeDisplayEditor phenotypeDisplayEditor;

    private PhenotypeEditEditor phenotypeEditEditor = new PhenotypeEditEditor();
    @UiField
    SimpleLayoutPanel lowerChartContainer;
    @UiField
    SimpleLayoutPanel upperChartContainer;
    @UiField
    HTMLPanel geoChartBtnContainer;
    @UiField
    HTMLPanel pieChartBtnContainer;
    @UiField
    HTMLPanel columnChartBtnContainer;
    @UiField
    HTMLPanel motionChartBtnContainer;
    @UiField
    Anchor edit;
    @UiField
    Anchor delete;
    @UiField(provided = true)
    MainResources mainRes;
    @UiField
    org.gwtbootstrap3.client.ui.NavPills statisticTypePills;
    @UiField
    LayoutPanel container;
    @UiField
    AnchorListItem navLinkPhenCSV;
    @UiField
    AnchorListItem navLinkPhenJSON;
    @UiField
    HTMLPanel actionBarPanel;
    @UiField
    HTMLPanel topRightPanel;
    @UiField
    HTMLPanel lowerPanel;
    protected DataTable histogramData;
    protected DataTable phenotypeExplorerData;
    protected DataTable geoChartData;
    protected DataTable phenotypeTypeData;
    private LOWER_CHART_TYPE lowerChartType = LOWER_CHART_TYPE.histogram;
    private UPPER_CHART_TYPE upperChartType = UPPER_CHART_TYPE.geochart;
    private ColumnChart columnChart = new ColumnChart();
    private MotionChart motionChart = new MotionChart();
    private GeoChart geoChart = new GeoChart();
    private PieChart pieChart = new PieChart();
    private final PhenotypeDisplayDriver displayDriver;
    private final PhenotypeEditDriver editDriver;
    private boolean layoutScheduled = false;
    private boolean showBlank = true;
    private Modal editPopup = new Modal();
    private DialogOptions deleteOptions = DialogOptions.newOptions("Do you rally want to delete the phenotype?");
    private ImmutableBiMap<StatisticTypeProxy, AnchorListItem> statisticTypeLinks;

    ClickHandler statisticTypeClickhandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            Anchor source = (Anchor) event.getSource();
            if (source.getParent() instanceof AnchorListItem) {
                AnchorListItem link = (AnchorListItem) source.getParent();
                if (!link.isEnabled())
                    return;
                resetStatisticTypeLinkActive();
                link.setActive(true);
                StatisticTypeProxy statisticType = statisticTypeLinks.inverse().get(link);
                getUiHandlers().onSelectStatisticType(statisticType);
            }
        }
    };


    private final ScheduledCommand layoutCmd = new ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };

    @Inject
    public PhenotypeDetailView(final Binder binder, final PhenotypeDisplayDriver displayDriver,
                               final PhenotypeEditDriver editDriver, final MainResources mainRes,
                               final OntologyManager ontologyManager, final PhenotypeDisplayEditor phenotypeDisplayEditor) {
        this.mainRes = mainRes;
        this.phenotypeDisplayEditor = phenotypeDisplayEditor;
        widget = binder.createAndBindUi(this);
        this.displayDriver = displayDriver;
        this.editDriver = editDriver;
        this.displayDriver.initialize(phenotypeDisplayEditor);
        this.editDriver.initialize(phenotypeEditEditor);
        phenotypeEditEditor.setOntologyManager(ontologyManager);
        editPopup.setDataBackdrop(ModalBackdrop.STATIC);
        editPopup.setClosable(true);
        editPopup.setTitle("Edit phenotype");
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
        modalBody.add(phenotypeEditEditor);
        editPopup.add(modalBody);
        editPopup.add(footer);

        deleteOptions.setTitle("Delete phenotype");
        deleteOptions.addButton("Cancel", ButtonType.DEFAULT.getCssName());
        deleteOptions.addButton("Delete", ButtonType.DANGER.getCssName(), new SimpleCallback() {
            @Override
            public void callback() {
                getUiHandlers().onConfirmDelete();
            }
        });

        actionBarPanel.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        topRightPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        lowerPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        edit.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onEdit();
            }
        }, ClickEvent.getType());
        delete.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onDelete();
            }
        }, ClickEvent.getType());
    }

    private void resetStatisticTypeLinkActive() {
        for (AnchorListItem link : statisticTypeLinks.values()) {
            link.setActive(false);
        }
    }

    private void resetStatisticTypeLinks() {
        for (Map.Entry<StatisticTypeProxy, AnchorListItem> entrySet : statisticTypeLinks.entrySet()) {
            AnchorListItem link = entrySet.getValue();
            StatisticTypeProxy statisticType = entrySet.getKey();
            link.setEnabled(false);
            link.setText(statisticType.getStatType());
            link.setActive(false);
        }
    }

    @Override
    public void setAvailableStatisticTypes(List<StatisticTypeProxy> statisticTypes) {
        statisticTypePills.clear();
        ImmutableBiMap.Builder builder = ImmutableBiMap.<StatisticTypeProxy, AnchorListItem>builder();
        for (StatisticTypeProxy statisticType : statisticTypes) {
            if (statisticType == null)
                continue;
            AnchorListItem link = new AnchorListItem(statisticType.getStatType());
            link.setEnabled(false);
            link.addClickHandler(statisticTypeClickhandler);
            builder.put(statisticType, link);
            statisticTypePills.add(link);
        }
        statisticTypeLinks = builder.build();
    }

    @Override
    public void setStatisticTypes(List<StatisticTypeProxy> statisticTypes) {
        resetStatisticTypeLinks();
        if (statisticTypes == null)
            return;
        for (int i = 0; i < statisticTypes.size(); i++) {
            StatisticTypeProxy statisticType = statisticTypes.get(i);
            AnchorListItem link = statisticTypeLinks.get(statisticType);
            if (link != null) {
                link.setText(statisticType.getStatType() + " [" + statisticType.getNumberOfTraits() + "]");
                link.setEnabled(true);
            }
        }
        if (statisticTypes.size() == 1) {
            StatisticTypeProxy statisticType = statisticTypes.get(0);
            statisticTypeLinks.get(statisticType).setActive(true);
            getUiHandlers().onSelectStatisticType(statisticType);
        }
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public PhenotypeDisplayDriver getDisplayDriver() {
        return displayDriver;
    }


    @Override
    public PhenotypeEditDriver getEditDriver() {
        return editDriver;
    }


    @Override
    public void setPhenotypeId(Long id) {
        navLinkPhenCSV.setHref(GWT.getHostPageBaseURL() + "/provider/phenotype/" + id + "/phenotypedata.csv");
        navLinkPhenJSON.setHref(GWT.getHostPageBaseURL() + "/provider/phenotype/" + id + "/phenotypedata.json");
    }

    @Override
    public void setAcceptableValuesForUnitOfMeasure(Collection<UnitOfMeasureProxy> values) {
        phenotypeEditEditor.setAcceptableValuesForUnitOfMeasure(values);
    }

    @Override
    public void setGeoChartData(Multiset<String> data) {
        geoChartData = DataTableUtils.createPhenotypeGeoChartTable(data);
    }

    @Override
    public void setHistogramChartData(
            ImmutableSortedMap<Double, Integer> data) {
        showBlank = data == null;
        this.histogramData = DataTableUtils.createPhenotypeHistogramTable(data);
    }

    @Override
    public void scheduledLayout() {
        if (widget.isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    @Override
    public void setPhenotypExplorerData(ImmutableList<TraitStatsProxy> traits) {
        phenotypeExplorerData = DataTableUtils.createPhentoypeExplorerTableFromStats(traits);
    }


    private void forceLayout() {
        if (!widget.isAttached() || !widget.isVisible())
            return;
        drawUpperCharts();
        drawLowerCharts();
        container.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.AUTO);
    }


    private GeoChartOptions createGeoChartOptions() {
        GeoChartOptions options = GeoChartOptions.create();
        return options;
    }

    private ColumnChartOptions createColumnChartOptions() {
        ColumnChartOptions options = DataTableUtils.getDefaultPhenotypeHistogramOptions();
        if (showBlank) {
            options.setTitle("Select a statistic type from the top list (measure, mean, variance, etc)");
            options.setColors("#CCC");
            Tooltip toolTip = Tooltip.create();
            toolTip.setTrigger(TooltipTrigger.NONE);
            options.setTooltip(toolTip);
            Legend legendOption = Legend.create();
            legendOption.setPosition(LegendPosition.NONE);
            options.setLegend(legendOption);
        }
        return options;
    }

    private MotionChartOptions createMotionChartOptions() {
        MotionChartOptions options = MotionChartOptions.create();
        options.setState(
                "%7B%22time%22%3A%22notime%22%2C%22iconType%22%3A%22BUBBLE%22%2C%22xZoomedDataMin%22%3Anull%2C%22yZoomedDataMax%22%3Anull%2C%22xZoomedIn%22%3Afalse%2C%22iconKeySettings%22%3A%5B%5D%2C%22showTrails%22%3Atrue%2C%22xAxisOption%22%3A%222%22%2C%22colorOption%22%3A%224%22%2C%22yAxisOption%22%3A%223%22%2C%22playDuration%22%3A15%2C%22xZoomedDataMax%22%3Anull%2C%22orderedByX%22%3Afalse%2C%22duration%22%3A%7B%22multiplier%22%3A1%2C%22timeUnit%22%3A%22none%22%7D%2C%22xLambda%22%3A1%2C%22orderedByY%22%3Afalse%2C%22sizeOption%22%3A%22_UNISIZE%22%2C%22yZoomedDataMin%22%3Anull%2C%22nonSelectedAlpha%22%3A0.4%2C%22stateVersion%22%3A3%2C%22dimensions%22%3A%7B%22iconDimensions%22%3A%5B%22dim0%22%5D%7D%2C%22yLambda%22%3A1%2C%22yZoomedIn%22%3Afalse%7D%3B");
        return options;
    }

    private PieChartOptions createPieChartOptions() {
        PieChartOptions options = PieChartOptions.create();
        options.setTitle("Geographic distribution");
        return options;
    }


    private void drawUpperCharts() {
        if (upperChartType == UPPER_CHART_TYPE.geochart) {
            if (upperChartContainer.getWidget() == null) {
                upperChartContainer.add(geoChart);
            } else {
                geoChart = (GeoChart) upperChartContainer.getWidget();
            }
            geoChart.draw(geoChartData, createGeoChartOptions());

        } else {
            if (upperChartContainer.getWidget() == null) {
                upperChartContainer.add(pieChart);
            } else {
                pieChart = (PieChart) upperChartContainer.getWidget();
            }
            pieChart.draw(geoChartData, createPieChartOptions());
        }
    }

    private void drawLowerCharts() {
        if (lowerChartType == LOWER_CHART_TYPE.histogram) {
            if (lowerChartContainer.getWidget() == null) {
                lowerChartContainer.add(columnChart);
            } else {
                columnChart = (ColumnChart) lowerChartContainer.getWidget();
            }
            columnChart.draw(histogramData, createColumnChartOptions());
        } else {
            if (lowerChartContainer.getWidget() != null) {
                lowerChartContainer.clear();
            }
            lowerChartContainer.add(motionChart);
            motionChart.draw(phenotypeExplorerData,
                    createMotionChartOptions());
        }
    }

    @UiHandler("pieChartBtn")
    public void onPieChartBtn(ClickEvent e) {
        if (upperChartType == UPPER_CHART_TYPE.piechart)
            return;
        upperChartType = UPPER_CHART_TYPE.piechart;
        geoChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        pieChartBtnContainer.addStyleName(mainRes.style()
                .iconContainer_active());
        upperChartContainer.clear();
        drawUpperCharts();
    }

    @UiHandler("geoChartBtn")
    public void onGeoChartBtn(ClickEvent e) {
        if (upperChartType == UPPER_CHART_TYPE.geochart)
            return;
        pieChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        geoChartBtnContainer.addStyleName(mainRes.style()
                .iconContainer_active());
        upperChartType = UPPER_CHART_TYPE.geochart;
        upperChartContainer.clear();
        drawUpperCharts();
    }

    @UiHandler("columnChartBtn")
    public void onColumnChartBtn(ClickEvent e) {
        if (lowerChartType == LOWER_CHART_TYPE.histogram)
            return;
        lowerChartType = LOWER_CHART_TYPE.histogram;
        motionChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        columnChartBtnContainer.addStyleName(mainRes.style()
                .iconContainer_active());
        lowerChartContainer.clear();
        drawLowerCharts();
    }

    @UiHandler("motionChartBtn")
    public void onMotionChartBtn(ClickEvent e) {
        if (lowerChartType == LOWER_CHART_TYPE.explorer)
            return;
        lowerChartType = LOWER_CHART_TYPE.explorer;
        columnChartBtnContainer.removeStyleName(mainRes.style()
                .iconContainer_active());
        motionChartBtnContainer.addStyleName(mainRes.style()
                .iconContainer_active());
        lowerChartContainer.clear();
        drawLowerCharts();
    }

    @Override
    public void drawCharts() {
        drawUpperCharts();
        drawLowerCharts();
    }

    @Override
    public void showActionBtns(boolean show) {
        edit.setVisible(show);
        delete.setVisible(show);
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
}
