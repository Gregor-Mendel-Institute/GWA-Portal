package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.client.editors.StudyDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.StudyEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyDetailPresenter;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.gmi.nordborglab.browser.client.util.DateUtils;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
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
	
	public interface StudyDisplayDriver extends RequestFactoryEditorDriver<StudyProxy, StudyDisplayEditor> {}
	public interface StudyEditDriver extends RequestFactoryEditorDriver<StudyProxy, StudyEditEditor> {}
	
	protected final StudyDisplayDriver displayDriver;
	protected final StudyEditDriver editDriver;
	
	protected State state = State.DISPLAYING;
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
	
	@UiField StudyDisplayEditor studyDisplayEditor;
	@UiField StudyEditEditor studyEditEditor;
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
	ToggleButton save;
	@UiField
	Anchor cancel;
	@UiField
	Anchor delete;
	@UiField(provided=true) MainResources mainRes;
    @UiField
    NavLink uploadBtn;
    @UiField
    NavLink startBtn;
    @UiField
    Label jobStatusLb;
    @UiField
    ProgressBar jobStatusProgress;
    @UiField
    com.google.gwt.user.client.ui.Label modifiedLb;
    @UiField
    com.google.gwt.user.client.ui.Label createdLb;
    @UiField
    com.google.gwt.user.client.ui.Label taskLb;
    @UiField
    NavPills actionDd;

    @Inject
	public StudyDetailView(final Binder binder, final StudyDisplayDriver displayDriver, final StudyEditDriver editDriver, final MainResources mainRes) {
		this.mainRes = mainRes;
		widget = binder.createAndBindUi(this);
		this.displayDriver = displayDriver;
		this.displayDriver.initialize(studyDisplayEditor);
		this.editDriver = editDriver;
		this.editDriver.initialize(studyEditEditor);
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
	public void setState(State state, int permission) {
		this.state = state;
		studyDisplayEditor.setVisible(state == State.DISPLAYING);
		studyEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		edit.setVisible(state == State.DISPLAYING && 
				(permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		delete.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
	}
	
	@Override
	public State getState() {
		return state;
	}

    @Override
    public void showGWASUploadPopup(boolean show) {
        if (show) {
            gwasUploadPopup.show();
        }
        else {
            gwasUploadPopup.hide();
        }

    }

    @Override
    public void showGWASBtns(boolean show) {
        actionDd.setVisible(show);
    }

    @Override
    public void showJobInfo(StudyJobProxy job, int permission) {
        String jobStatusText ="";
        LabelType jobLabelType = null;
        ProgressBar.Color progresBarColor = null;
        String jobTask = "";
        boolean showJobActionBtns = false;
        boolean showProgress = false;
        Integer progress = 0;
        if (job == null) {
            jobStatusText = "NA";
            jobLabelType = LabelType.DEFAULT;
            showProgress = false;
            showJobActionBtns = true;
        }
        else {
            jobStatusText = job.getStatus();
            jobTask = job.getTask();
            progress = job.getProgress();
            jobStatusLb.setText(job.getStatus());
            if (job.getStatus().equalsIgnoreCase("Finished")) {
                jobLabelType = LabelType.SUCCESS;
                showProgress = false;
            }
            else if (job.getStatus().equalsIgnoreCase("Queued")) {
                jobLabelType = LabelType.IMPORTANT;
                showProgress = true;
                progresBarColor = ProgressBarBase.Color.DANGER;
            }
            else if (job.getStatus().equalsIgnoreCase("Running")) {
                jobLabelType = LabelType.WARNING;
                showProgress = true;
                progresBarColor = ProgressBarBase.Color.WARNING;
            }
            else if (job.getStatus().equalsIgnoreCase("Error")) {
                jobLabelType = LabelType.IMPORTANT;
                showProgress = false;
            }
            Long currentTimeMillis = System.currentTimeMillis();
            if (job.getCreateDate() != null)  {
                createdLb.setText(DateUtils.formatTimeElapsedSinceMillisecond(currentTimeMillis - job.getCreateDate().getTime()) + " ago");
            }
            if (job.getModificationDate() != null)  {
                modifiedLb.setText(DateUtils.formatTimeElapsedSinceMillisecond(currentTimeMillis-job.getModificationDate().getTime())+ " ago");
            }
        }
        jobStatusProgress.setPercent(progress);
        jobStatusProgress.setVisible(showProgress);
        jobStatusProgress.setColor(progresBarColor);
        jobStatusProgress.setText(progress.toString());
        jobStatusLb.setText(jobStatusText);
        jobStatusLb.setType(jobLabelType);
        taskLb.setText(jobTask);
        actionDd.setVisible(state == State.DISPLAYING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT && showJobActionBtns);
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
		options.setHeight(600);
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
        gwasUploadPopup.setMaxHeigth(widget.getOffsetHeight()+"px");
        gwasUploadPopup.setWidth(widget.getOffsetWidth());
        gwasUploadPopup.show();
    }

    @UiHandler("startBtn")
    public void onClickStartBtn(ClickEvent e)  {
        getUiHandlers().onStartAnalysis();
    }
	
	@UiHandler("edit") 
	public void onEdit(ClickEvent e){
		if (state == State.DISPLAYING) {
			getUiHandlers().onEdit();
		}
	}
	
	@UiHandler("delete")
	public void onDelete(ClickEvent e) {
		if (state == State.EDITING) {
			if (Window.confirm("Do you really want to delete the Phenotype and all the studies?")) 
				getUiHandlers().onDelete();
		}
	}
	
	@UiHandler("save") 
	public void onSave(ClickEvent e) {
		if (state == State.EDITING) {
			getUiHandlers().onSave();
		}
	}
	
	@UiHandler("cancel") 
	public void onCancel(ClickEvent e) {
		if (state == State.EDITING) {
			getUiHandlers().onCancel();
		}
	}

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == StudyDetailPresenter.TYPE_SetGWASUploadContent) {
            gwasUploadPopup.add(content);
        }
        else  {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
