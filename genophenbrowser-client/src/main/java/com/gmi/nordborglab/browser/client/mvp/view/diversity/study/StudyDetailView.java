package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.gmi.nordborglab.browser.client.editors.StudyDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.StudyEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyDetailPresenter;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.CircularProgressBar;
import com.gmi.nordborglab.browser.client.ui.EnableDropdownButton;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.client.util.DateUtils;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Map;

public class StudyDetailView extends ViewWithUiHandlers<StudyDetailUiHandlers> implements
        StudyDetailPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, StudyDetailView> {
    }

    private final ScheduledCommand layoutCmd = new ScheduledCommand() {
        public void execute() {
            layoutScheduled = false;
            forceLayout();
        }
    };

    public interface StudyDisplayDriver extends RequestFactoryEditorDriver<StudyProxy, StudyDisplayEditor> {
    }

    public interface StudyEditDriver extends RequestFactoryEditorDriver<StudyProxy, StudyEditEditor> {
    }

    protected final StudyDisplayDriver displayDriver;
    protected final StudyEditDriver editDriver;

    private boolean layoutScheduled = false;

    public enum LOWER_CHART_TYPE {
        histogram, explorer
    }

    public enum UPPER_CHART_TYPE {
        geochart, piechart
    }

    protected DataTable histogramData;
    protected DataTable phenotypeExplorerData;
    protected DataTable geoChartData;
    private LOWER_CHART_TYPE lowerChartType = LOWER_CHART_TYPE.histogram;
    private UPPER_CHART_TYPE upperChartType = UPPER_CHART_TYPE.geochart;
    private ResizeableColumnChart columnChart;
    private ResizeableMotionChart motionChart;
    private GeoChart geoChart = new GeoChart();
    private PieChart pieChart;
    private Modal gwasUploadPopup = new Modal(true);

    private Modal editPopup = new Modal(true);
    private Modal deletePopup = new Modal(true);

    @UiField(provided = true)
    StudyDisplayEditor studyDisplayEditor;
    private StudyEditEditor studyEditEditor = new StudyEditEditor();
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
    ToggleButton edit;
    @UiField
    ToggleButton delete;
    @UiField(provided = true)
    MainResources mainRes;
    @UiField
    NavLink uploadBtn;
    @UiField
    NavLink startBtn;

    /*@UiField
    Label jobStatusLb;*/


    /*@UiField
    ProgressBar jobStatusProgress;*/
    @UiField
    com.google.gwt.user.client.ui.Label modifiedLb;
    @UiField
    com.google.gwt.user.client.ui.Label createdLb;
    @UiField
    com.google.gwt.user.client.ui.Label taskLb;

    /*@UiField
    NavPills actionDd;
    */

    @UiField
    CircularProgressBar jobProgress;
    @UiField
    EnableDropdownButton jobNABtn;
    @UiField
    EnableDropdownButton jobERRORBtn;
    @UiField
    EnableDropdownButton jobFinishedBtn;
    @UiField
    Button jobWaitingBtn;
    @UiField
    HTMLPanel gwasJobContainer;

    @Inject
    public StudyDetailView(final Binder binder, final StudyDisplayDriver displayDriver, final StudyEditDriver editDriver,
                           final MainResources mainRes, final StudyDisplayEditor studyDisplayEditor) {
        this.mainRes = mainRes;
        this.studyDisplayEditor = studyDisplayEditor;
        widget = binder.createAndBindUi(this);
        this.displayDriver = displayDriver;
        this.displayDriver.initialize(studyDisplayEditor);
        this.editDriver = editDriver;
        this.editDriver.initialize(studyEditEditor);
        editPopup.setBackdrop(BackdropType.STATIC);
        editPopup.setCloseVisible(true);
        editPopup.setTitle("Edit analysis");
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
        editPopup.add(studyEditEditor);
        editPopup.add(footer);

        deletePopup.setBackdrop(BackdropType.STATIC);
        deletePopup.setCloseVisible(true);
        deletePopup.add(new HTML("<h4>Do you really want to delete the analysis?</h4>"));
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

        gwasJobContainer.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        gwasJobContainer.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        Map<Range<Integer>, String> colorRanges = ImmutableMap.<Range<Integer>, String>builder()
                .put(Range.closedOpen(0, 1), "rgba(0,0,0,0.4)")
                .put(Range.closedOpen(1, 99), "#faa732")
                .put(Range.closed(100, 100), "#5bb75b")
                .build();
        jobProgress.setThresholds(colorRanges);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public StudyDisplayDriver getDisplayDriver() {
        return displayDriver;
    }

    @Override
    public StudyEditDriver getEditDriver() {
        return editDriver;
    }


    @Override
    public void showActionBtns(boolean show) {
        edit.setVisible(show);
        delete.setVisible(show);
    }

    @Override
    public void showGWASUploadPopup(boolean show) {
        if (show) {
            gwasUploadPopup.show();
        } else {
            gwasUploadPopup.hide();
        }

    }

    @Override
    public void showJobInfo(StudyJobProxy job, int permission) {
        boolean hasPermission = (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT;
        jobERRORBtn.setVisible(false);
        jobNABtn.setVisible(false);
        jobFinishedBtn.setVisible(false);
        jobWaitingBtn.setVisible(false);
        createdLb.setVisible(false);
        modifiedLb.setVisible(false);

        String jobTask = "";
        boolean showJobActionBtns = false;
        boolean showProgress = false;
        Integer progress = 0;
        jobProgress.setHasError(false);
        if (job == null) {
            showProgress = false;
            showJobActionBtns = true;
            progress = 0;
            if (hasPermission) {
                jobTask = "Click on N/A to start";
            }
            jobNABtn.setVisible(true);
            jobNABtn.setEnable(hasPermission);
        } else {
            jobTask = job.getTask();
            progress = job.getProgress();
            //jobStatusLb.setText(job.getStatus());
            if (job.getStatus().equalsIgnoreCase("Finished")) {
                jobFinishedBtn.setVisible(true);
                jobFinishedBtn.setEnable(hasPermission);
            } else if (job.getStatus().equalsIgnoreCase("Waiting")) {
                jobWaitingBtn.setVisible(true);
                jobWaitingBtn.setText("Waiting");
            } else if (job.getStatus().equalsIgnoreCase("Pending")) {
                jobWaitingBtn.setVisible(true);
                jobWaitingBtn.setText("Pending");
            } else if (job.getStatus().equalsIgnoreCase("Running")) {
                jobWaitingBtn.setVisible(true);
                jobWaitingBtn.setText("Running");
            } else if (job.getStatus().equalsIgnoreCase("Error")) {
                jobERRORBtn.setVisible(true);
                jobERRORBtn.setEnable(hasPermission);
                jobProgress.setHasError(true);
            }
            Long currentTimeMillis = System.currentTimeMillis();
            if (job.getCreateDate() != null) {
                createdLb.setVisible(true);
                createdLb.setText("Created: " + DateUtils.formatTimeElapsedSinceMillisecond(currentTimeMillis - job.getCreateDate().getTime(), 1) + " ago");
            }
            if (job.getModificationDate() != null) {
                modifiedLb.setVisible(true);
                modifiedLb.setText("Modified: " + DateUtils.formatTimeElapsedSinceMillisecond(currentTimeMillis - job.getModificationDate().getTime(), 1) + " ago");
            }
        }
        jobProgress.setProgress(progress);
        taskLb.setText(jobTask);
    }


    private void forceLayout() {
        if (!widget.isAttached() || !widget.isVisible())
            return;
        drawUpperCharts();
        drawLowerCharts();
    }


    @Override
    public void scheduledLayout() {
        if (widget.isAttached() && !layoutScheduled) {
            layoutScheduled = true;
            Scheduler.get().scheduleDeferred(layoutCmd);
        }
    }

    private GeoChart.Options createGeoChart() {
        GeoChart.Options options = GeoChart.Options.create();
        options.setTitle("Geographic distribution");
        options.setHeight(upperChartContainer.getOffsetHeight());
        return options;
    }

    private Options createColumnChartOptions() {
        Options options = Options.create();
        options.setTitle("Phenotype Histogram");
        Options animationOptions = Options.create();
        animationOptions.set("duration", 1000.0);
        animationOptions.set("easing", "out");
        options.set("animation", animationOptions);
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

    @Override
    public void setHistogramChartData(ImmutableSortedMap<Double, Integer> data) {
        this.histogramData = DataTable.create();
        histogramData.addColumn(ColumnType.STRING, "Bin");
        histogramData.addColumn(ColumnType.NUMBER, "Frequency");
        histogramData.addRows(data.size() - 1);
        ImmutableList<Double> keys = data.keySet().asList();
        ImmutableList<Integer> values = data.values().asList();
        for (int i = 0; i < data.size() - 1; i++) {
            histogramData.setValue(i, 0, keys.get(i) + " - " + keys.get(i + 1));
            histogramData.setValue(i, 1, values.get(i));
        }
    }

    @Override
    public void setPhenotypExplorerData(ImmutableSet<TraitProxy> traits) {
        this.phenotypeExplorerData = DataTableUtils.createPhentoypeExplorerTable(traits.asList());
    }

    @Override
    public void setGeoChartData(Multiset<String> data) {
        geoChartData = DataTable.create();
        geoChartData.addColumn(ColumnType.STRING, "Country");
        geoChartData.addColumn(ColumnType.NUMBER, "Frequency");
        for (String cty : data.elementSet()) {
            int i = geoChartData.addRow();
            geoChartData.setValue(i, 0, cty);
            geoChartData.setValue(i, 1, data.count(cty));
        }
    }

    private void drawUpperCharts() {
        upperChartContainer.clear();
        if (upperChartType == UPPER_CHART_TYPE.geochart) {
            upperChartContainer.add(geoChart);
            geoChart.draw(geoChartData, createGeoChart());
        } else {
            pieChart = new PieChart(geoChartData, createPieChartOptions());
            upperChartContainer.add(pieChart);
        }
    }

    private void drawLowerCharts() {
        lowerChartContainer.clear();
        if (lowerChartType == LOWER_CHART_TYPE.histogram) {
            columnChart = new ResizeableColumnChart(histogramData,
                    createColumnChartOptions());
            lowerChartContainer.add(columnChart);
        } else {
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
        drawLowerCharts();
    }

    @UiHandler("uploadBtn")
    public void onClickUploadBtn(ClickEvent e) {
        getUiHandlers().onClickUpload();
        gwasUploadPopup.setMaxHeigth(widget.getOffsetHeight() + "px");
        gwasUploadPopup.setWidth(widget.getOffsetWidth());
        gwasUploadPopup.show();
    }


    @UiHandler({"deleteJobBtn", "deleteFinishedJobBtn"})
    public void onClickDeleteJobBtn(ClickEvent e) {
        getUiHandlers().onDeleteJob();
    }

    @UiHandler("rerunJobBtn")
    public void onClickRerunJobBtn(ClickEvent e) {
        getUiHandlers().onReRunAnalysis();
    }

    @UiHandler("startBtn")
    public void onClickStartBtn(ClickEvent e) {
        getUiHandlers().onStartAnalysis();
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
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == StudyDetailPresenter.TYPE_SetGWASUploadContent) {
            gwasUploadPopup.add(content);
        } else {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
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
