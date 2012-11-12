package com.gmi.nordborglab.browser.client.mvp.view.germplasm.taxonomy;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;

import com.gmi.nordborglab.browser.client.editors.TaxonomyDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.TaxonomyEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.TaxonomyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.taxonomy.TaxonomyDetailPresenter;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomyDetailView extends ViewWithUiHandlers<TaxonomyDetailUiHandlers> implements
		TaxonomyDetailPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, TaxonomyDetailView> {
	}
	
	public interface TaxonomyDisplayDriver extends RequestFactoryEditorDriver<TaxonomyProxy, TaxonomyDisplayEditor> {}
	public interface TaxonomyEditDriver extends RequestFactoryEditorDriver<TaxonomyProxy, TaxonomyEditEditor> {}
	
	@UiField TaxonomyDisplayEditor taxonomyDisplayEditor;
	@UiField TaxonomyEditEditor taxonomyEditEditor;
	@UiField ToggleButton edit; 
	@UiField ToggleButton save;
	@UiField Anchor cancel;
	@UiField SimpleLayoutPanel lowerChartContainer;
	@UiField SimpleLayoutPanel lowerLeftChartContainer;
	@UiField SimpleLayoutPanel upperLeftChartContainer;
	@UiField SimpleLayoutPanel upperRightChartContainer;
	
	protected State state = State.DISPLAYING;
	private final TaxonomyDisplayDriver displayDriver;
	private final TaxonomyEditDriver editDriver;
	private boolean layoutScheduled = false;
	private DataTable geoChartData;
	private DataTable alleleAssayData;
	private DataTable sampStatData;
	private DataTable stockGenerationData;
	
	private final ScheduledCommand layoutCmd = new ScheduledCommand() {
		public void execute() {
			layoutScheduled = false;
			forceLayout();
		}
	};

	@Inject
	public TaxonomyDetailView(final Binder binder, final TaxonomyDisplayDriver displayDriver, final TaxonomyEditDriver editDriver) {
		widget = binder.createAndBindUi(this);
		this.displayDriver = displayDriver;
		this.editDriver = editDriver;
		this.displayDriver.initialize(taxonomyDisplayEditor);
		this.editDriver.initialize(taxonomyEditEditor);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public TaxonomyDisplayDriver getDisplayDriver() {
		return displayDriver;
	}
	
	@Override
	public TaxonomyEditDriver getEditDriver() {
		return editDriver;
	}

	@Override
	public void setState(State state, int permission) {
		this.state = state;
		taxonomyDisplayEditor.setVisible(state == State.DISPLAYING);
		taxonomyEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		edit.setVisible(state == State.DISPLAYING && 
				(permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
	}
	
	
	@UiHandler("edit") 
	public void onEdit(ClickEvent e){
		if (state == State.DISPLAYING) {
			getUiHandlers().onEdit();
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
	public void setGeoChartData(DataTable geoChartData) {
		this.geoChartData = geoChartData;
		
	}

	@Override
	public void setAlleleAssayData(DataTable alleleAssayData) {
		this.alleleAssayData = alleleAssayData;
	}

	@Override
	public void setSampStatData(DataTable sampStatData) {
		this.sampStatData = sampStatData;
	}
	
	@Override
	public void setStockGenerationData(DataTable stockGenerationData) {
		this.stockGenerationData = stockGenerationData;
		
	}

	@Override
	public void scheduleLayout() {
		if (widget.isAttached() && !layoutScheduled) {
			layoutScheduled = true;
			Scheduler.get().scheduleDeferred(layoutCmd);
		}
	}
	
	private void forceLayout() {
		if (!widget.isAttached() || !widget.isVisible())
			return;
		drawAlleleAssayChart();
		drawGeoChart();
		drawSampStatChart();
		drawStockDataChart();
	}
	
	private GeoChart.Options createGeoChartOptions() {
		GeoChart.Options options = GeoChart.Options.create();
		options.setTitle("Geographic distribution");
		options.setWidth(lowerChartContainer.getOffsetWidth());
		options.setHeight(lowerChartContainer.getOffsetHeight());
		options.set("keepAspectRatio",false);
		return options;
	}
	
	private void drawStockDataChart() {
		PieOptions options = PieOptions.create();
		//options.setTitle("Geographic distribution");
		options.setHeight(lowerLeftChartContainer.getOffsetHeight());
		options.setWidth(lowerLeftChartContainer.getOffsetWidth());
		if (lowerLeftChartContainer.getWidget() == null) {
			lowerLeftChartContainer.add(new PieChart(stockGenerationData, options));
		}
		else {
			PieChart pieChart  = (PieChart)lowerLeftChartContainer.getWidget();
			pieChart.draw(stockGenerationData,options);
		}
	}
	
	private void drawGeoChart() {
		if (lowerChartContainer.getWidget() == null) {
			lowerChartContainer.add(new GeoChart(geoChartData,createGeoChartOptions()));
		}
		else {
			GeoChart geoChart = (GeoChart)lowerChartContainer.getWidget();
			geoChart.draw(geoChartData,createGeoChartOptions());
		}
	}
	private void drawAlleleAssayChart() {
		PieOptions options = PieOptions.create();
		options.setHeight(upperLeftChartContainer.getOffsetHeight());
		options.setWidth(upperLeftChartContainer.getOffsetWidth());
		if (upperLeftChartContainer.getWidget() == null) {
			upperLeftChartContainer.add(new PieChart(alleleAssayData, options));
		}
		else {
			PieChart pieChart  = (PieChart)upperLeftChartContainer.getWidget();
			pieChart.draw(alleleAssayData,options);
		}
	}
	
	private void drawSampStatChart() {
		PieOptions options = PieOptions.create();
		options.setHeight(upperRightChartContainer.getOffsetHeight());
		options.setWidth(upperRightChartContainer.getOffsetWidth());
		if (upperRightChartContainer.getWidget() == null) {
			upperRightChartContainer.add(new PieChart(sampStatData, options));
		}
		else {
			PieChart pieChart  = (PieChart)upperRightChartContainer.getWidget();
			pieChart.draw(sampStatData,options);
		}
	}

	
	
}
