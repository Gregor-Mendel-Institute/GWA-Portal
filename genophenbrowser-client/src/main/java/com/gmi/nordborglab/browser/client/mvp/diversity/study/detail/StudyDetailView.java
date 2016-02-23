package com.gmi.nordborglab.browser.client.mvp.diversity.study.detail;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;
import com.gmi.nordborglab.browser.client.editors.StudyDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.StudyEditEditor;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.CircularProgressBar;
import com.gmi.nordborglab.browser.client.ui.PlotDownloadPopup;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.client.util.DateUtils;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.util.Normality;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
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
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.SimpleCallback;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;

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
    private Modal gwasUploadPopup = new Modal();
    private ModalBody gwasUploadPopupContent = new ModalBody();
    private Modal plotsPopup = new Modal();
    private Modal editPopup = new Modal();
    private DialogOptions deleteOptions = DialogOptions.newOptions("Do you really want to delete the analysis?");
    private PlotDownloadPopup plotsPanel = new PlotDownloadPopup(PlotDownloadPopup.PLOT_TYPE.STUDY);

    private Double shapiroWilkPvalue;
    private Double pseudoHeritability;

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
    Anchor edit;
    @UiField
    Anchor delete;
    @UiField(provided = true)
    MainResources mainRes;
    @UiField
    AnchorListItem uploadBtn;
    @UiField
    AnchorListItem startBtn;

    @UiField
    Label modifiedLb;
    @UiField
    Label createdLb;
    @UiField
    Label taskLb;

    @UiField
    CircularProgressBar jobProgress;
    @UiField
    Button jobNABtn;
    @UiField
    Button jobERRORBtn;
    @UiField
    Button jobFinishedBtn;
    @UiField
    org.gwtbootstrap3.client.ui.Button jobWaitingBtn;
    @UiField
    HTMLPanel gwasJobContainer;
    @UiField
    HTMLPanel actionBarPanel;
    @UiField
    LayoutPanel topLeftPanel;
    @UiField
    AnchorListItem navLinkPvalCSV;
    @UiField
    AnchorListItem navLinkPvalHDF5;
    @UiField
    AnchorListItem navLinkPvalJSON;
    @UiField
    org.gwtbootstrap3.client.ui.Divider downloadDivider;
    @UiField
    AnchorListItem navLinkPhenCSV;
    @UiField
    AnchorListItem navLinkPhenJSON;
    @UiField
    HTMLPanel topRightPanel;
    @UiField
    HTMLPanel lowerPanel;
    @UiField
    LayoutPanel container;


    @Inject
    public StudyDetailView(final Binder binder, final StudyDisplayDriver displayDriver, final StudyEditDriver editDriver,
                           final MainResources mainRes, final StudyDisplayEditor studyDisplayEditor) {
        this.mainRes = mainRes;
        this.studyDisplayEditor = studyDisplayEditor;
        widget = binder.createAndBindUi(this);

        gwasUploadPopup.add(gwasUploadPopupContent);
        bindSlot(StudyDetailPresenter.SLOT_GWAS_UPLOAD, gwasUploadPopupContent);


        this.displayDriver = displayDriver;
        this.displayDriver.initialize(studyDisplayEditor);
        this.editDriver = editDriver;
        this.editDriver.initialize(studyEditEditor);


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
        ModalFooter footer = new ModalFooter();
        footer.add(cancelEditBtn);
        footer.add(saveEditBtn);
        ModalBody modalBody = new ModalBody();
        modalBody.add(studyEditEditor);
        editPopup.add(modalBody);
        editPopup.add(footer);
        gwasUploadPopup.setTitle("Upload GWAS result");

        plotsPopup.setTitle("Download GWAS plots");
        modalBody = new ModalBody();
        modalBody.add(plotsPanel);
        plotsPopup.add(modalBody);


        deleteOptions.setTitle("Delete analysis");
        deleteOptions.addButton("Cancel", ButtonType.DEFAULT.getCssName());
        deleteOptions.addButton("Delete", ButtonType.DANGER.getCssName(), new SimpleCallback() {
            @Override
            public void callback() {
                getUiHandlers().onConfirmDelete();
            }
        });
        gwasJobContainer.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        gwasJobContainer.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getParentElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        actionBarPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        topRightPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
        lowerPanel.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);

        Map<Range<Integer>, String> colorRanges = ImmutableMap.<Range<Integer>, String>builder()
                .put(Range.closedOpen(0, 1), "rgba(0,0,0,0.4)")
                .put(Range.closedOpen(1, 99), "#faa732")
                .put(Range.closed(100, 100), "#5bb75b")
                .build();
        jobProgress.setThresholds(colorRanges);

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
        boolean showDownloadLinks = true;
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
            jobNABtn.setEnabled(hasPermission);
        } else {
            jobTask = job.getTask();
            progress = job.getProgress();
            showDownloadLinks = true;
            //jobStatusLb.setText(job.getStatus());
            if (job.getStatus().equalsIgnoreCase("Finished")) {
                jobFinishedBtn.setVisible(true);
                jobFinishedBtn.setEnabled(hasPermission);
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
                jobERRORBtn.setEnabled(hasPermission);
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
        navLinkPvalCSV.setVisible(showDownloadLinks);
        downloadDivider.setVisible(showDownloadLinks);
        navLinkPvalHDF5.setVisible(showDownloadLinks);
        navLinkPvalJSON.setVisible(showDownloadLinks);
    }


    private void forceLayout() {
        if (!widget.isAttached() || !widget.isVisible())
            return;
        drawUpperCharts();
        drawLowerCharts();
        // can't be in costructor because getParentElement() is null
        container.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.AUTO);
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

    private String getTitle() {
        String title = "Phenotype Histogram (shapiroWilkPvalue: " + getRoundedValue(shapiroWilkPvalue) + " | pseudoHerit.: " + getRoundedValue(pseudoHeritability) + ")";
        return title;
    }

    private String getRoundedValue(Double value) {
        if (value == null)
            return "N/A";
        value = Normality.getRoundedValue(value);
        if (value == Double.NaN)
            return "Infinity";
        return String.valueOf(value);
    }

    private Options createColumnChartOptions() {
        Options options = Options.create();
        options.setTitle(getTitle());
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
        this.plotsPanel.setMaxMac(traits.size());
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
        gwasUploadPopup.setWidth(widget.getOffsetWidth() + "px");
        gwasUploadPopup.show();
    }

    @UiHandler("plotsLink")
    public void onClickPlotsLink(ClickEvent e) {
        plotsPopup.show();
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

    @Override
    public void setStudyId(Long id) {
        navLinkPvalCSV.setHref(GWT.getHostPageBaseURL() + "/provider/study/" + id + "/pvalues.csv");
        navLinkPvalHDF5.setHref(GWT.getHostPageBaseURL() + "/provider/study/" + id + "/pvalues.hdf5");
        navLinkPvalJSON.setHref(GWT.getHostPageBaseURL() + "/provider/study/" + id + "/pvalues.json");
        navLinkPhenCSV.setHref(GWT.getHostPageBaseURL() + "/provider/study/" + id + "/phenotypedata.csv");
        navLinkPhenJSON.setHref(GWT.getHostPageBaseURL() + "/provider/study/" + id + "/phenotypedata.json");
        plotsPanel.setId(id);
    }

    @Override
    public void setStats(Double pseudoHeritability, Double shapiroWilkPvalue) {
        this.shapiroWilkPvalue = shapiroWilkPvalue;
        this.pseudoHeritability = pseudoHeritability;
    }
}
