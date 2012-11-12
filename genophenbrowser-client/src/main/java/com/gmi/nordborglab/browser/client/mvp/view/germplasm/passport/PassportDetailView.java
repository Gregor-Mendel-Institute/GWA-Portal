package com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport;




import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.DataGrid;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.editors.PassportDisplayEditor;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportDetailPresenter;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.HyperlinkCell;
import com.gmi.nordborglab.browser.client.util.CustomDataTable;
import com.gmi.nordborglab.browser.client.util.CustomDataTable.Filter;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.MouseEvent;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.visualization.client.DataView;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class PassportDetailView extends ViewImpl implements
		PassportDetailPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, PassportDetailView> {
	}
	
	public interface PassportDisplayDriver extends RequestFactoryEditorDriver<PassportProxy, PassportDisplayEditor> {}
	
	@UiField(provided=true) DataGrid<StockProxy> stockDataGrid;
	@UiField CustomPager  stockPager;
	
	@UiField(provided=true) DataGrid<PhenotypeProxy> phenotypeDataGrid;
	@UiField CustomPager  phenotypePager;
	
	@UiField(provided=true) DataGrid<StudyProxy> studyDataGrid;
	@UiField CustomPager  studyPager;
	
	@UiField PassportDisplayEditor passportDisplayEditor;
	@UiField ToggleButton edit; 
	@UiField ToggleButton save;
	@UiField Anchor cancel;
	@UiField SimpleLayoutPanel mapContainer;
	@UiField Label stockStatsLabel;
	@UiField Label phenotypeStatsLabel;
	@UiField Label studyStatsLabel;
	@UiField Label genotypeStatsLabel;
	@UiField SimpleLayoutPanel statsChartContainer;
	@UiField Button traitOBtn;
	@UiField Button envOBtn;
	@UiField Button statisticTypeBtn;
	@UiField Button unitOfMeasureBtn;
	
	
	public enum STATS_TYPE {TRAIT_ONTOLOGY,ENV_ONTOLOGY,STATISTIC_TYPE,UNIT_OF_MEASURE}
	
	
	private final ScheduledCommand layoutCmd = new ScheduledCommand() {
		public void execute() {
			layoutScheduled = false;
			forceLayout();
		}
	};
	
	
	protected State state = State.DISPLAYING;
	private final PassportDisplayDriver displayDriver;
	private MapWidget mapWidget;
	private Marker marker = Marker.newInstance(null);
	private final PlaceManager placeManager;
	private CustomDataTable statsDataTable = null;
	private boolean layoutScheduled = false;

	@Inject
	public PassportDetailView(final Binder binder, final PassportDisplayDriver displayDriver, 
			 final PlaceManager placeManager) {
		this.placeManager = placeManager;
	
		
		phenotypeDataGrid = new DataGrid<PhenotypeProxy>(10,new EntityProxyKeyProvider<PhenotypeProxy>());
		phenotypeDataGrid.setWidth("100%");
		phenotypeDataGrid.setStriped(false);
		
		studyDataGrid = new DataGrid<StudyProxy>(10,new EntityProxyKeyProvider<StudyProxy>());
		studyDataGrid.setWidth("100%");
		
		
		stockDataGrid = new DataGrid<StockProxy>(10,new EntityProxyKeyProvider<StockProxy>());
		stockDataGrid.setWidth("100%");
		stockDataGrid.setStriped(true);
		widget = binder.createAndBindUi(this);
		stockPager.setDisplay(stockDataGrid);
		phenotypePager.setDisplay(phenotypeDataGrid);
		studyPager.setDisplay(studyDataGrid);
		this.displayDriver = displayDriver;
		this.displayDriver.initialize(passportDisplayEditor);
		marker.addClickHandler(new ClickMapHandler() {

			@Override
			public void onEvent(ClickMapEvent event) {
				drawInfoWindow(marker,event.getMouseEvent());
			}
		});
		initStockDataGrid();
		initPhenotypeDataGrid();
		initStudyDataGrid();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public PassportDisplayDriver getDisplayDriver() {
		return displayDriver;
	}

	@Override
	public void setState(State state, int permission) {
		this.state = state;
		passportDisplayEditor.setVisible(state == State.DISPLAYING);
		//taxonomyEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		edit.setVisible(state == State.DISPLAYING && 
				(permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
		cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.WRITE) == AccessControlEntryProxy.WRITE);
	}
	
	@Override
	public void initMap() {
		
		if (mapWidget != null)
			return;
		MapOptions opts = MapOptions.newInstance();
		opts.setZoom(3);
		opts.setMapTypeId(MapTypeId.TERRAIN);
		mapWidget = new MapWidget(opts);
		mapWidget.setSize("100%", "100%");
		mapContainer.add(mapWidget);
		marker.setMap(mapWidget);
	}
	
	@Override
	public void showPassportOnMap(PassportProxy passport) {
		if (passport.getCollection() != null && passport.getCollection().getLocality() != null) {
			LocalityProxy locality = passport.getCollection().getLocality() ;
			LatLng position = LatLng.newInstance(locality.getLatitude(), locality.getLongitude());
			marker.setPosition(position);
			marker.setTitle(passport.getAccename());
			mapWidget.setCenter(position);
		}
	}
	
	private void drawInfoWindow(Marker marker,MouseEvent mouseEvent)  {
		if (marker == null || mouseEvent == null) {
			return;
		}

		HTML html = new HTML("You clicked on: "
				+ mouseEvent.getLatLng().getToString());

		InfoWindowOptions options = InfoWindowOptions.newInstance();
		options.setContent(html);
		InfoWindow iw = InfoWindow.newInstance(options);
		iw.open(mapWidget, marker);
	}
	
	private void initStockDataGrid() {
		final PlaceRequest placeRequest = new ParameterizedPlaceRequest(NameTokens.stock);
		stockDataGrid.addColumn(new Column<StockProxy, String[]>(new HyperlinkCell()) {

			@Override
			public String[] getValue(StockProxy object) {
				String[] hyperlink = new String[2];
				hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
				hyperlink[HyperlinkCell.URL_INDEX] = object.getId().toString();
				return hyperlink;
			}
		},"ID");
		
		stockDataGrid.addColumn(new Column<StockProxy,String> (new TextCell()) {

			@Override
			public String getValue(StockProxy object) {
				String generation = "";
				if (object.getGeneration() != null) 
					generation = object.getGeneration().getComments();
				return generation;
			}
		},"Generation");
		stockDataGrid.addColumn(new Column<StockProxy,String> (new TextCell()) {

			@Override
			public String getValue(StockProxy object) {
				return object.getSeedLot();
			}
		},"Seed lot");
		stockDataGrid.addColumn(new Column<StockProxy,String> (new TextCell()) {

			@Override
			public String getValue(StockProxy object) {
				return object.getStockSource();
			}
		},"Stock source");
	}
	
	private void initPhenotypeDataGrid() {
		final PlaceRequest placeRequest = new ParameterizedPlaceRequest(NameTokens.phenotype);
		phenotypeDataGrid.addColumn(new Column<PhenotypeProxy, String[]>(new HyperlinkCell()) {

			@Override
			public String[] getValue(PhenotypeProxy object) {
				String[] hyperlink = new String[2];
				hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
				hyperlink[HyperlinkCell.URL_INDEX] = object.getId().toString();
				return hyperlink;
			}
		},"ID");
		phenotypeDataGrid.addColumn(new Column<PhenotypeProxy,String> (new TextCell()) {

			@Override
			public String getValue(PhenotypeProxy object) {
				return object.getLocalTraitName();
			}
		},"Name");
		phenotypeDataGrid.addColumn(new Column<PhenotypeProxy,String> (new TextCell()) {

			@Override
			public String getValue(PhenotypeProxy object) {
				String unitOfMeasure = "";
				if (object.getUnitOfMeasure() != null) 
					unitOfMeasure = object.getUnitOfMeasure().getUnitType();
				return unitOfMeasure;
			}
		},"Unit");
		
		phenotypeDataGrid.addColumn(new Column<PhenotypeProxy,String> (new TextCell()) {

			@Override
			public String getValue(PhenotypeProxy object) {
				return object.getTraitProtocol();
			}
		},"Comments");
	}
	
	private void initStudyDataGrid() {
		final PlaceRequest placeRequest = new ParameterizedPlaceRequest(NameTokens.study);
		studyDataGrid.addColumn(new Column<StudyProxy, String[]>(new HyperlinkCell()) {

			@Override
			public String[] getValue(StudyProxy object) {
				String[] hyperlink = new String[2];
				hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
				hyperlink[HyperlinkCell.URL_INDEX] = object.getId().toString();
				return hyperlink;
			}
		},"ID");
		studyDataGrid.addColumn(new Column<StudyProxy,String> (new TextCell()) {

			@Override
			public String getValue(StudyProxy object) {
				return object.getName();
			}
		},"Name");
		studyDataGrid.addColumn(new Column<StudyProxy,String> (new TextCell()) {

			@Override
			public String getValue(StudyProxy object) {
				String protocol = "";
				if (object.getProtocol() != null) 
					protocol = object.getProtocol().getAnalysisMethod();
				return protocol;
			}
		},"Protocol");
		
		studyDataGrid.addColumn(new Column<StudyProxy,String> (new TextCell()) {

			@Override
			public String getValue(StudyProxy object) {
				String genotype = "";
				if (object.getAlleleAssay() != null) 
					genotype = object.getAlleleAssay().getName();
				return genotype;
			}
		},"Genotype");
	}

	@Override
	public HasData<StockProxy> getStockDataDisplay() {
		return stockDataGrid;
	}
	
	@Override
	public HasData<StudyProxy> getStudyDataDisplay() {
		return studyDataGrid;
	}
	
	
	@Override
	public HasData<PhenotypeProxy> getPhenotypeDataDisplay() {
		return phenotypeDataGrid;
	}
	
	

	@Override
	public HasText getStockStatsLabel() {
		return stockStatsLabel;
	}

	@Override
	public HasText getPhenotypeStatsLabel() {
		return phenotypeStatsLabel;
	}

	@Override
	public HasText getStudyStatsLabel() {
		return studyStatsLabel;
	}

	@Override
	public HasText getGenotypeStatsLabel() {
		return genotypeStatsLabel;
	}

	@Override
	public void setStatsDataTable(CustomDataTable statsDataTable) {
		this.statsDataTable = statsDataTable;
		
	}
	
	private void forceLayout() {
		if (!widget.isAttached() || !widget.isVisible())
			return;
		drawStatsChart(STATS_TYPE.TRAIT_ONTOLOGY);
	}

	@Override
	public void scheduleLayout() {
		if (widget.isAttached() && !layoutScheduled) {
			layoutScheduled = true;
			Scheduler.get().scheduleDeferred(layoutCmd);
		}
	}
	
	private void drawStatsChart(STATS_TYPE type) {
		if (statsDataTable == null)
			return;
		DataView view = DataView.create(statsDataTable);
		int[] columns = new int[2];
		columns[0]= 0;
		columns[1] = 1;
		view.setColumns(columns);
		///TODO add JSNI call to getFilteredRows to custom DataTable
		JsArray<Filter> filters = JsArray.createArray().cast();
		Filter filter = Filter.createObject().cast();
		filter.setColumn(2);
		filter.setValue(type.ordinal());
		filters.push(filter);
		view.setRows(statsDataTable.getFilteredRows(filters));
		PieOptions options = PieOptions.create();
		options.setHeight(statsChartContainer.getOffsetHeight());
		options.setWidth(statsChartContainer.getOffsetWidth());
		//Options animationOptions = Options.create();
		/*animationOptions.set("duration", 1000.0);
		animationOptions.set("easing", "out");
		options.set("animation", animationOptions);*/
		String title = "";
		switch (type) {
			case TRAIT_ONTOLOGY:
				title = "Trait - Ontologies";
				break;
			case ENV_ONTOLOGY:
				title = "Environment - Ontologies";
				break;
			case STATISTIC_TYPE:
				title = "Statistic -Type";
				break;
			case UNIT_OF_MEASURE:
				title = "Unit of measure" ;
				break;
		}
		options.setTitle(title);
		if (statsChartContainer.getWidget() == null) {
			statsChartContainer.add(new PieChart(view, options));
		}
		else {
			PieChart pieChart  = (PieChart)statsChartContainer.getWidget();
			pieChart.draw(view,options);
		}
		
		
	}
	
	@UiHandler({"statisticTypeBtn","unitOfMeasureBtn","envOBtn","traitOBtn"})
	public void onStatsToggleButtonClick(ClickEvent e) {
		Widget sender = (Widget) e.getSource();
		STATS_TYPE type = null;
		if (sender ==  statisticTypeBtn) 
			type = STATS_TYPE.STATISTIC_TYPE;
		else if (sender == unitOfMeasureBtn)
			type  = STATS_TYPE.UNIT_OF_MEASURE;
		else if (sender == envOBtn)
			type = STATS_TYPE.ENV_ONTOLOGY;
		else if (sender == traitOBtn)
			type = STATS_TYPE.TRAIT_ONTOLOGY;
			
		if (type != null)
			drawStatsChart(type);
	}
}
