package com.gmi.nordborglab.browser.client.ui.card;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.gmi.nordborglab.browser.client.events.SelectTransformationEvent;
import com.gmi.nordborglab.browser.client.resources.CardRendererResources;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.shared.proxy.TransformationProxy;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 10:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransformationCard extends Composite implements RequiresResize {



    interface TransformationCardUiBinder extends UiBinder<FocusPanel, TransformationCard> {
    }

    private static TransformationCardUiBinder ourUiBinder = GWT.create(TransformationCardUiBinder.class);
    protected DataTable histogramData;
    private ResizeableColumnChart chart;
    private EventBus eventBus;

    protected boolean isSelected;
    private TransformationProxy transformation;

    private Double shapiroScore;
    @UiField
    SpanElement shapiroScoreLb;
    @UiField
    SimpleLayoutPanel histogramContainer;
    @UiField
    DivElement card;
    @UiField
    HeadingElement titleLb;

    @UiField
    Icon selectIcon;

    @UiField
    FocusPanel focusPanel;
    @UiField
    CardRendererResources cardRen;
    private boolean layoutScheduled = false;
    private final Scheduler.ScheduledCommand layoutCmd = new Scheduler.ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };

    public TransformationCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void onResize() {
        initChartContainerHeight();
        histogramContainer.onResize();
    }


    public void setHistogramData(ImmutableSortedMap<Double, Integer> data, Double shapiroScore) {
        setHistogramData(DataTableUtils.createPhenotypeHistogramTable(data), shapiroScore);
    }

    public void setHistogramData(DataTable histogramData, Double shapiroScore) {
        this.histogramData = histogramData;
        this.shapiroScore = shapiroScore;
        shapiroScoreLb.setInnerText(shapiroScore.toString());
        scheduledLayout();
    }


    private Options createChartOptions() {
        Options options = Options.create();
        options.setTitle("");
        Options animationOptions = Options.create();
        animationOptions.set("duration", 1000.0);
        animationOptions.set("easing", "out");
        //options.setHeight(getOffsetHeight() - 40);
        options.set("animation", animationOptions);
        return options;
    }

    private void drawHistogram(DataTable data, Options options) {
        if (chart == null) {
            chart = new ResizeableColumnChart(data, options);
            histogramContainer.add(chart);
        } else
            chart.draw2(data, options);
    }

    private void forceLayout() {
        if (!isAttached() || !isVisible())
            return;
        drawHistogram(histogramData, createChartOptions());
    }

    public void scheduledLayout() {
        if (isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }


    private void initChartContainerHeight() {
        if (!isAttached() || !isVisible())
            return;
        int availableHeight = getOffsetHeight() - 120;
        if (availableHeight > 0)
            histogramContainer.setHeight(availableHeight + "px");
    }

    private void updateSelected() {
        if (isSelected) {
            card.addClassName(cardRen.style().card_selected());
            selectIcon.removeStyleName(cardRen.style().empty_ok());
            selectIcon.addStyleName(cardRen.style().ok());
            selectIcon.setType(IconType.OK);

        } else {
            card.removeClassName(cardRen.style().card_selected());
            selectIcon.removeStyleName(cardRen.style().ok());
            selectIcon.addStyleName(cardRen.style().empty_ok());
            selectIcon.setType(IconType.OK_CIRCLE);
        }

    }

    public void setSelected(boolean isSelected) {
        if (isSelected != this.isSelected) {
            this.isSelected = isSelected;
            updateSelected();
        }
    }

    public TransformationProxy getTransformation() {
        return transformation;
    }

    public void setTransformation(TransformationProxy transformation) {
        this.transformation = transformation;
        titleLb.setInnerText(transformation.getName() + " transformation");
    }


    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @UiHandler("focusPanel")
    public void onClick(ClickEvent e) {
        SelectTransformationEvent.fire(eventBus, this);
    }

    public DataTable getHistogramData() {
        return histogramData;
    }

    public Double getShapiroScore() {
        return shapiroScore;
    }

    public void setChartHeight(String height) {
        histogramContainer.setHeight(height);
    }

    public void setChartWidth(String width) {
        histogramContainer.setWidth(width);
    }
}