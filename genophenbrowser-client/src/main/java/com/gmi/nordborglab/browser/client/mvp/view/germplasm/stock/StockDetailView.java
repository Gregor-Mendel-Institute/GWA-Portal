package com.gmi.nordborglab.browser.client.mvp.view.germplasm.stock;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.editors.StockDisplayEditor;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.stock.StockDetailPresenter;
import com.gmi.nordborglab.browser.client.util.CustomDataTable;
import com.gmi.nordborglab.browser.client.util.CustomDataTable.Filter;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.DataView;
import com.google.gwt.visualization.client.visualizations.OrgChart;
import com.google.gwt.visualization.client.visualizations.OrgChart.Size;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class StockDetailView extends ViewImpl implements
		StockDetailPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, StockDetailView> {
	}
	
	public interface StockDisplayDriver extends RequestFactoryEditorDriver<StockProxy, StockDisplayEditor>{}

	private final ScheduledCommand layoutCmd = new ScheduledCommand() {
		public void execute() {
			layoutScheduled = false;
			forceLayout();
		}
	};

	@UiField
	SimpleLayoutPanel chartUpperContainer;
	@UiField
	SimpleLayoutPanel chartLowerContainer;
	@UiField StockDisplayEditor stockDisplayEditor;
	
	private CustomDataTable pedigreeData = null;
	private boolean layoutScheduled = false;
	private final StockDisplayDriver displayDriver;
	private final PlaceManager placeManager;

	@Inject
	public StockDetailView(final Binder binder, final StockDisplayDriver displayDriver, final PlaceManager placeManager) {
		widget = binder.createAndBindUi(this);
		this.displayDriver =displayDriver;
		this.placeManager = placeManager;
		this.displayDriver.initialize(stockDisplayEditor);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPedigreeData(CustomDataTable pedigreeData) {
		this.pedigreeData = pedigreeData;
		formatDataTable();
		forceLayout();
	}

	private void drawPedigreeChart() {
		if (pedigreeData == null)
			return;
		OrgChart.Options options = OrgChart.Options.create();
		options.setAllowHtml(true);
		options.setSize(Size.LARGE);
		options.setAllowCollapse(true);
		DataView ancestorsView = DataView.create(pedigreeData);
		DataView descendentsView = DataView.create(pedigreeData);
		
		ancestorsView.setRows(pedigreeData.getFilteredRows(getFilters(0)));
		descendentsView.setRows(pedigreeData.getFilteredRows(getFilters(1)));
	

		if (chartUpperContainer.getWidget() == null)
			chartUpperContainer.add(new OrgChart(ancestorsView, options));
		else {
			OrgChart orgChart = (OrgChart) chartUpperContainer.getWidget();
			orgChart.draw(ancestorsView, options);
		}
		if (chartLowerContainer.getWidget() == null)
			chartLowerContainer.add(new OrgChart(descendentsView, options));
		else {
			OrgChart orgChart = (OrgChart) chartLowerContainer.getWidget();
			orgChart.draw(descendentsView, options);
		}
	}

	private void forceLayout() {
		if (!widget.isAttached() || !widget.isVisible())
			return;
		drawPedigreeChart();
	}

	@Override
	public void scheduleLayout() {
		if (widget.isAttached() && !layoutScheduled) {
			layoutScheduled = true;
			Scheduler.get().scheduleDeferred(layoutCmd);
		}
	}
	
	private JsArray<Filter> getFilters(int value) {
		Filter filter = Filter.createObject().cast();
		filter.setColumn(3);
		filter.setValue(value);
		JsArray<Filter> filters = JsArray.createArray().cast();
		filters.push(filter);
		return filters;
	}
	
	@Override
	public StockDisplayDriver getDisplayDriver() {
		return displayDriver;
	}
	
	private void formatDataTable() {
		if (pedigreeData == null)
			return;
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.stock);
		String formattedText = "";
		for (int i = 0;i<pedigreeData.getNumberOfRows();i++) {
			String id = pedigreeData.getValueString(i, 0);
			String role =  pedigreeData.getValueString(i, 2);
			request = request.with("id",id);
			formattedText = "<a href='#"+ placeManager.buildHistoryToken(request)+"'>"+id+"</a><br>" + role;
			pedigreeData.setFormattedValue(i, 0, formattedText);
		}
	}
}
