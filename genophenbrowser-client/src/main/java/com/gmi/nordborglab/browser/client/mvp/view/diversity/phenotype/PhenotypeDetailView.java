package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import java.util.Collection;
import java.util.Set;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;

import com.gmi.nordborglab.browser.client.editors.PhenotypeDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.PhenotypeEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyDetailPresenter.LOWER_CHART_TYPE;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyDetailPresenter.UPPER_CHART_TYPE;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.ui.ResizeableMotionChart;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.UnitOfMeasureProxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.gwt.core.client.JsArray;
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
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.PieChart.PieLegendPosition;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class PhenotypeDetailView extends ViewWithUiHandlers<PhenotypeDetailUiHandlers> implements
		PhenotypeDetailPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, PhenotypeDetailView> {
	}
	public interface PhenotypeDisplayDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeDisplayEditor> {}
	public interface PhenotypeEditDriver extends RequestFactoryEditorDriver<PhenotypeProxy, PhenotypeEditEditor> {}
	
	@UiField PhenotypeDisplayEditor phenotypeDisplayEditor;
	@UiField PhenotypeEditEditor phenotypeEditEditor;
	@UiField
	SimpleLayoutPanel lowerChartContainer;
	@UiField
	SimpleLayoutPanel upperChartContainer;
	@UiField SimpleLayoutPanel phenotypePieChartContainer;
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
	protected DataTable histogramData;
	protected DataTable phenotypeExplorerData;
	protected DataTable geoChartData;
	protected DataTable phenotypeTypeData;
	private LOWER_CHART_TYPE lowerChartType = LOWER_CHART_TYPE.histogram;
	private UPPER_CHART_TYPE upperChartType = UPPER_CHART_TYPE.geochart;
	private ColumnChart columnChart;
	private ResizeableMotionChart motionChart;
	//private ResizeablePieChart 
	private GeoChart geoChart = new GeoChart();
	private PieChart pieChart;
	private PieChart phenotypePieChart;
	protected State state = State.DISPLAYING;
	private final PhenotypeDisplayDriver displayDriver;
	private final PhenotypeEditDriver editDriver;
	private boolean layoutScheduled = false;
	private boolean showBlank = true;
	
	
	private final ScheduledCommand layoutCmd = new ScheduledCommand() {
		public void execute() {
			layoutScheduled = false;
			forceLayout();
		}
	};

	@Inject
	public PhenotypeDetailView(final Binder binder,final PhenotypeDisplayDriver displayDriver, final PhenotypeEditDriver editDriver,final MainResources mainRes) {
		this.mainRes = mainRes;
		widget = binder.createAndBindUi(this);
		this.displayDriver = displayDriver;
		this.editDriver = editDriver;
		this.displayDriver.initialize(phenotypeDisplayEditor);
		this.editDriver.initialize(phenotypeEditEditor); 
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
	public void setState(State state,int permission) {
		this.state = state;
		phenotypeDisplayEditor.setVisible(state == State.DISPLAYING);
		phenotypeEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		edit.setVisible(state == State.DISPLAYING && 
				(permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		delete.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.DELETE) == AccessControlEntryProxy.DELETE);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public PhenotypeEditDriver getEditDriver() {
		return editDriver;
	}
	
	@Override
	public void setAcceptableValuesForUnitOfMeasure(Collection<UnitOfMeasureProxy> values) {
		phenotypeEditEditor.setAcceptableValuesForUnitOfMeasure(values);
	}

	@Override
	public void setGeoChartData(Multiset<String> data) {
		geoChartData = DataTable.create();
		geoChartData.addColumn(ColumnType.STRING, "Country");
		geoChartData.addColumn(ColumnType.NUMBER, "Frequency");
		if (data != null) {
			for (String cty : data.elementSet()) {
				int i = geoChartData.addRow();
				geoChartData.setValue(i, 0, cty);
				geoChartData.setValue(i, 1, data.count(cty));
			}
		}
		
	}

	@Override
	public void setHistogramChartData(
			ImmutableSortedMap<Double, Integer> data) {
		this.histogramData = DataTable.create();
		histogramData.addColumn(ColumnType.STRING, "Bin");
		histogramData.addColumn(ColumnType.NUMBER, "Frequency");
		if (data != null) {
			histogramData.addRows(data.size() - 1);
			ImmutableList<Double> keys = data.keySet().asList();
			ImmutableList<Integer> values = data.values().asList();
			for (int i = 0; i < data.size() - 1; i++) {
				histogramData.setValue(i, 0, keys.get(i) + " - " + keys.get(i + 1));
				histogramData.setValue(i, 1, values.get(i));
			}
			showBlank = false;
		}
		else {
			histogramData.addRows(3);
			histogramData.setValue(0, 0, "A");
			histogramData.setValue(0, 1, 5);
			histogramData.setValue(1, 0, "B");
			histogramData.setValue(1, 1, 10);
			histogramData.setValue(2, 0, "C");
			histogramData.setValue(2, 1, 7);
			showBlank = true;
		}
	}

	@Override
	public void scheduledLayout() {
		if (widget.isAttached() && !layoutScheduled) {
			layoutScheduled = true;
			Scheduler.get().scheduleDeferred(layoutCmd);
		}
	}

	@Override
	public void setPhenotypExplorerData(ImmutableList<TraitProxy> traits) {
		phenotypeExplorerData = DataTable.create();
		phenotypeExplorerData.addColumn(ColumnType.STRING, "ID Name Phenotype");
		phenotypeExplorerData.addColumn(ColumnType.NUMBER, "Date");
		phenotypeExplorerData.addColumn(ColumnType.NUMBER, "Longitude");
		phenotypeExplorerData.addColumn(ColumnType.NUMBER, "Latitude");
		phenotypeExplorerData.addColumn(ColumnType.NUMBER, "Phenotype");
		phenotypeExplorerData.addColumn(ColumnType.STRING, "Accession");
		phenotypeExplorerData.addColumn(ColumnType.STRING, "Country");
		if (traits != null) {
			phenotypeExplorerData.addRows(traits.size());
			int i = 0;
			String name = "";
			String accession = "";
			Double longitude = null;
			Double latitude = null;
			Double phenotype = null;
			String country = "";
			for (TraitProxy trait : traits) {
				try {
					PassportProxy passport = trait.getObsUnit().getStock()
							.getPassport();
					accession = passport.getAccename();
					name = accession + " ID:" + passport.getId() + " Phenotype:"
							+ trait.getValue();
					if (trait.getValue() != null && !trait.getValue().equals(""))
						phenotype = Double.parseDouble(trait.getValue());
					LocalityProxy locality = trait.getObsUnit().getStock()
							.getPassport().getCollection().getLocality();
					longitude = locality.getLongitude();
					latitude = locality.getLatitude();
					country = locality.getCountry();
	
				} catch (Exception e) {
					
				}
	
				phenotypeExplorerData.setValue(i, 0, name);
				phenotypeExplorerData.setValue(i, 1, 1900);
				if (longitude != null)
					phenotypeExplorerData.setValue(i, 2, longitude);
				if (latitude != null)
					phenotypeExplorerData.setValue(i, 3, latitude);
				if (phenotype != null)
					phenotypeExplorerData.setValue(i, 4, phenotype);
				phenotypeExplorerData.setValue(i, 5, accession);
				phenotypeExplorerData.setValue(i, 6, country);
				i = i + 1;
			}
		}
	}
	
	private void forceLayout() {
		if (!widget.isAttached() || !widget.isVisible())
			return;
		drawPhenotypePieChart();
		drawUpperCharts();
		drawLowerCharts();
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
	
	private GeoChart.Options createGeoChartOptions() {
		GeoChart.Options options = GeoChart.Options.create();
		options.setTitle("Geographic distribution");
		options.setHeight(upperChartContainer.getOffsetHeight());
		return options;
	}

	private Options createColumnChartOptions() {
		Options options = Options.create();
		options.setTitle("Phenotype Histogram");
		Options animationOptions = Options.create();
		if (showBlank) {
			options.setTitle("<- Select a phenotype type from the piechart");
			options.setColors("#CCC");
			Options toolTip = Options.create();
			toolTip.set("trigger", "none");
			options.set("tooltip", toolTip);
			Options legendOption = Options.create();
			legendOption.set("position", "none");
			options.set("legend", legendOption);
		}
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

	private void drawPhenotypePieChart() {
		PieOptions options = PieOptions.create();
		options.setTitle("Select a phenotype type");
		
		options.setLegend(PieLegendPosition.BOTTOM);
		//options.setHeight(phenotype.getOffsetHeight());
		//options.setWidth(upperChartContainer.getOffsetWidth());
		//return options;
		options.setAutomaticResize(true);
		phenotypePieChartContainer.clear();
		phenotypePieChart = new PieChart(phenotypeTypeData,options);
		phenotypePieChartContainer.add(phenotypePieChart);
		
		phenotypePieChart.addSelectHandler( new SelectHandler() {

			@Override
			public void onSelect(SelectEvent event) {
				JsArray<Selection> selections =  phenotypePieChart.getSelections();
				
				Integer row = null;
				if (selections != null && selections.length() > 0) {
					Selection selection = selections.get(0);
					 row = selection.getRow();
				}
				getUiHandlers().onSelectPhenotypeType(row);
			}
			
		});
	}
	
	

	private void drawUpperCharts() {
		if (upperChartType == UPPER_CHART_TYPE.geochart) {
			if (upperChartContainer.getWidget() == null) {
				upperChartContainer.add(geoChart);
				geoChart.draw(geoChartData, createGeoChartOptions());
			}
			else {
				geoChart = (GeoChart)upperChartContainer.getWidget();
				geoChart.draw(geoChartData,createGeoChartOptions());
			}
		} else {
			if (upperChartContainer.getWidget() == null) { 
				pieChart = new PieChart(geoChartData, createPieChartOptions());
				upperChartContainer.add(pieChart);
			}
			else {
				pieChart = (PieChart) upperChartContainer.getWidget();
				pieChart.draw(geoChartData,createPieChartOptions());
			}
		}
	}

	private void drawLowerCharts() {
		if (lowerChartType == LOWER_CHART_TYPE.histogram) {
			if (lowerChartContainer.getWidget() == null) {
				columnChart = new ResizeableColumnChart(histogramData,
						createColumnChartOptions());
				lowerChartContainer.add(columnChart);
			}
			else {
				columnChart = (ColumnChart)lowerChartContainer.getWidget();
				columnChart.draw(histogramData,createColumnChartOptions());
			}
		} else {
			if (lowerChartContainer.getWidget() == null) {
				motionChart = new ResizeableMotionChart(phenotypeExplorerData,
					createMotionChartOptions());
				lowerChartContainer.add(motionChart);
			}
			else  {
				motionChart = (ResizeableMotionChart)lowerChartContainer.getWidget();
				motionChart.draw(phenotypeExplorerData,createMotionChartOptions());
			}
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
	public void setPhenotypePieChartData(Set<StatisticTypeProxy> statisticTypes) {
		phenotypeTypeData = DataTable.create();
		phenotypeTypeData.addColumn(ColumnType.STRING, "Type");
		phenotypeTypeData.addColumn(ColumnType.NUMBER, "Values");
		for (StatisticTypeProxy type: statisticTypes) {
			int i = phenotypeTypeData.addRow();
			phenotypeTypeData.setValue(i, 0, type.getStatType());
			phenotypeTypeData.setValue(i, 1, type.getNumberOfTraits());
		}
	}
	
	@Override
	public void drawCharts() {
		drawUpperCharts();
		drawLowerCharts();
	}
}
