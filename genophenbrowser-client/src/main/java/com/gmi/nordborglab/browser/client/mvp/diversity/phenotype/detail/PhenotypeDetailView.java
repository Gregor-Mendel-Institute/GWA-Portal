package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.gmi.nordborglab.browser.client.editors.PhenotypeDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.manager.OntologyManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.detail.StudyDetailPresenter.LOWER_CHART_TYPE;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.detail.StudyDetailPresenter.UPPER_CHART_TYPE;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitStatsProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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
    Icon edit;
    @UiField
    Icon delete;
    @UiField(provided = true)
    MainResources mainRes;
    @UiField
    NavPills statisticTypePills;
    @UiField
    LayoutPanel container;
    @UiField
    NavLink navLinkPhenCSV;
    @UiField
    NavLink navLinkPhenJSON;
    @UiField
    HTMLPanel actionBarPanel;
    protected DataTable histogramData;
    protected DataTable phenotypeExplorerData;
    protected DataTable geoChartData;
    protected DataTable phenotypeTypeData;
    private LOWER_CHART_TYPE lowerChartType = LOWER_CHART_TYPE.histogram;
    private UPPER_CHART_TYPE upperChartType = UPPER_CHART_TYPE.geochart;
    private ResizeableColumnChart columnChart;
    private ResizeableMotionChart motionChart;
    //private ResizeablePieChart
    private GeoChart geoChart = new GeoChart();
    private PieChart pieChart;
    private PieChart phenotypePieChart;
    private final PhenotypeDisplayDriver displayDriver;
    private final PhenotypeEditDriver editDriver;
    private boolean layoutScheduled = false;
    private boolean showBlank = true;
    private Modal editPopup = new Modal(true);
    private Modal deletePopup = new Modal(true);
    private ImmutableBiMap<StatisticTypeProxy, NavLink> statisticTypeLinks;

    ClickHandler statisticTypeClickhandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            IconAnchor source = (IconAnchor) event.getSource();
            if (source.getParent() instanceof NavLink) {
                NavLink link = (NavLink) source.getParent();
                if (link.isDisabled())
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
        editPopup.setBackdrop(BackdropType.STATIC);
        editPopup.setCloseVisible(true);
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
        ModalFooter footer = new ModalFooter(cancelEditBtn, saveEditBtn);
        editPopup.add(phenotypeEditEditor);
        editPopup.add(footer);

        deletePopup.setBackdrop(BackdropType.STATIC);
        deletePopup.setCloseVisible(true);
        deletePopup.add(new HTML("<h4>Do you really want to delete the phenotype?</h4>"));
        Button cancelDeleteBtn = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                deletePopup.hide();
            }
        });
        cancelDeleteBtn.setType(ButtonType.DEFAULT);
        Button deleteBtn = new Button("Delete", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onConfirmDelete();
            }
        });
        deleteBtn.setType(ButtonType.DANGER);
        deletePopup.add(new ModalFooter(cancelDeleteBtn, deleteBtn));
        actionBarPanel.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
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
        for (NavLink link : statisticTypeLinks.values()) {
            link.setActive(false);
        }
    }

    private void resetStatisticTypeLinks() {
        for (Map.Entry<StatisticTypeProxy, NavLink> entrySet : statisticTypeLinks.entrySet()) {
            NavLink link = entrySet.getValue();
            StatisticTypeProxy statisticType = entrySet.getKey();
            link.setDisabled(true);
            link.setText(statisticType.getStatType());
            link.setActive(false);
        }
    }

    @Override
    public void setAvailableStatisticTypes(List<StatisticTypeProxy> statisticTypes) {
        statisticTypePills.clear();
        ImmutableBiMap.Builder builder = ImmutableBiMap.<StatisticTypeProxy, NavLink>builder();
        for (StatisticTypeProxy statisticType : statisticTypes) {
            if (statisticType == null)
                continue;
            NavLink link = new NavLink(statisticType.getStatType());
            link.setDisabled(true);
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
            NavLink link = statisticTypeLinks.get(statisticType);
            if (link != null) {
                link.setText(statisticType.getStatType() + " [" + statisticType.getNumberOfTraits() + "]");
                link.setDisabled(false);
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
        navLinkPhenCSV.setHref("/provider/phenotype/" + id + "/phenotypedata.csv");
        navLinkPhenJSON.setHref("/provider/phenotype/" + id + "/phenotypedata.json");
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
        //drawPhenotypePieChart();
        drawUpperCharts();
        drawLowerCharts();
        container.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.AUTO);
    }




    private GeoChart.Options createGeoChartOptions() {
        GeoChart.Options options = GeoChart.Options.create();
        options.setTitle("Geographic distribution");
        options.setHeight(upperChartContainer.getOffsetHeight());
        return options;
    }

    private Options createColumnChartOptions() {
        Options options = DataTableUtils.getDefaultPhenotypeHistogramOptions();
        if (showBlank) {
            options.setTitle("Select a statistic type from the top list (measure, mean, variance, etc)");
            options.setColors("#CCC");
            Options toolTip = Options.create();
            toolTip.set("trigger", "none");
            options.set("tooltip", toolTip);
            Options legendOption = Options.create();
            legendOption.set("position", "none");
            options.set("legend", legendOption);
        }
        return options;
    }

    private MotionChart.Options createMotionChartOptions() {
        MotionChart.Options options = MotionChart.Options.create();
        options.set(
                "state",
                "%7B%22time%22%3A%22notime%22%2C%22iconType%22%3A%22BUBBLE%22%2C%22xZoomedDataMin%22%3Anull%2C%22yZoomedDataMax%22%3Anull%2C%22xZoomedIn%22%3Afalse%2C%22iconKeySettings%22%3A%5B%5D%2C%22showTrails%22%3Atrue%2C%22xAxisOption%22%3A%222%22%2C%22colorOption%22%3A%224%22%2C%22yAxisOption%22%3A%223%22%2C%22playDuration%22%3A15%2C%22xZoomedDataMax%22%3Anull%2C%22orderedByX%22%3Afalse%2C%22duration%22%3A%7B%22multiplier%22%3A1%2C%22timeUnit%22%3A%22none%22%7D%2C%22xLambda%22%3A1%2C%22orderedByY%22%3Afalse%2C%22sizeOption%22%3A%22_UNISIZE%22%2C%22yZoomedDataMin%22%3Anull%2C%22nonSelectedAlpha%22%3A0.4%2C%22stateVersion%22%3A3%2C%22dimensions%22%3A%7B%22iconDimensions%22%3A%5B%22dim0%22%5D%7D%2C%22yLambda%22%3A1%2C%22yZoomedIn%22%3Afalse%7D%3B");
        options.setHeight(lowerChartContainer.getOffsetHeight());
        options.setWidth(lowerChartContainer.getOffsetWidth());
        return options;
    }

    private Options createPieChartOptions() {
        PieOptions options = PieOptions.create();
        options.setTitle("Geographic distribution");
        options.setHeight(upperChartContainer.getOffsetHeight());
        options.setWidth(upperChartContainer.getOffsetWidth());
        return options;
    }


    private void drawUpperCharts() {
        if (upperChartType == UPPER_CHART_TYPE.geochart) {
            if (upperChartContainer.getWidget() == null) {
                upperChartContainer.add(geoChart);
                geoChart.draw(geoChartData, createGeoChartOptions());
            } else {
                geoChart = (GeoChart) upperChartContainer.getWidget();
                geoChart.draw(geoChartData, createGeoChartOptions());
            }
        } else {
            if (upperChartContainer.getWidget() == null) {
                pieChart = new PieChart(geoChartData, createPieChartOptions());
                upperChartContainer.add(pieChart);
            } else {
                pieChart = (PieChart) upperChartContainer.getWidget();
                pieChart.draw(geoChartData, createPieChartOptions());
            }
        }
    }

    private void drawLowerCharts() {
        if (lowerChartType == LOWER_CHART_TYPE.histogram) {
            if (lowerChartContainer.getWidget() == null) {
                columnChart = new ResizeableColumnChart(histogramData,
                        createColumnChartOptions());
                lowerChartContainer.add(columnChart);
            } else {
                columnChart = (ResizeableColumnChart) lowerChartContainer.getWidget();
                columnChart.draw2(histogramData, createColumnChartOptions());
            }
        } else {
            if (lowerChartContainer.getWidget() != null) {
                lowerChartContainer.clear();
            }
            motionChart = new ResizeableMotionChart(phenotypeExplorerData,
                    createMotionChartOptions());
            lowerChartContainer.add(motionChart);
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
    public void showDeletePopup(boolean show) {
        if (show)
            deletePopup.show();
        else
            deletePopup.hide();
    }
}
