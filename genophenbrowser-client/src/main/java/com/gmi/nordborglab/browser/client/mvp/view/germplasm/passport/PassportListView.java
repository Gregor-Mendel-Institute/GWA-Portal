package com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport;


import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.DataGrid.SelectableResources;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.PassportListViewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportListPresenter.PassportProxyFilter;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.AccNameColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.AccNumberColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.AlleleAssayColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.CollDateColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.CollectorColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.CountryColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.IdColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.SourceColumn;
import com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport.PassportDataGridColumns.TypeColumn;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampStatProxy;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.event.ChosenChangeEvent.ChosenChangeHandler;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class PassportListView extends ViewWithUiHandlers<PassportListViewUiHandlers> implements
		PassportListPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, PassportListView> {
	}
	
	@UiField(provided=true) DataGrid<PassportProxy> passportDataGrid;
	@UiField CustomPager  pager;
	@UiField TextBox filterName;
	@UiField TextBox filterCollector;
	@UiField TextBox filterAccNumber;
	@UiField TextBox filterCountry;
	@UiField TextBox filterSource;
	@UiField TextBox filterId;
	@UiField HTMLPanel northContainer;
	@UiField(provided=true) ChosenListBox filterSampStat;
	@UiField(provided=true) ChosenListBox  filterAlleleAssay;
	@UiField SimpleLayoutPanel mapContainer;
	private MapWidget mapWidget;
	private static ProvidesKey<PassportProxy> KEY_PROVIDER = new EntityProxyKeyProvider<PassportProxy>();
	private SingleSelectionModel<PassportProxy> selectionModel = new SingleSelectionModel<PassportProxy>(KEY_PROVIDER);
	//@UiField(provided=true) ChosenListBox filterCountryLb;
	
	private final FlagMap flagMap;
	private Marker marker = Marker.newInstance(null);
	
	private static int SEARCH_INTERVAL = 1000;
	private boolean isSearchTimerStarted = false;
	private PassportProxyFilter passportProxyFilter;
	private final PlaceManager placeManager;
	
	 private final Timer searchTimer = new Timer() {
		    @Override
		    public void run() {
		    	isSearchTimerStarted = false;
		    	updateSearchFilter();
		    	getUiHandlers().onStartSearch();
		    }
		  };

	@Inject
	public PassportListView(final Binder binder, final FlagMap flagMap, 
			final SelectableResources dataGridResources, final PlaceManager placeManager ) {
		this.flagMap = flagMap;
		this.placeManager = placeManager;
		dataGridResources.dataGridStyle().ensureInjected();
		passportDataGrid = new DataGrid<PassportProxy>(10,dataGridResources,KEY_PROVIDER);
		passportDataGrid.setSelectionModel(selectionModel);
		passportDataGrid.setWidth("100%");
		passportDataGrid.setStriped(true);
		filterSampStat = new ChosenListBox(false);
		filterAlleleAssay = new ChosenListBox(true);
		widget = binder.createAndBindUi(this);
		//Because of this bug https://github.com/jDramaix/gwtchosen/issues/5
		northContainer.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
		pager.setDisplay(passportDataGrid);
		filterSampStat.setAllowSingleDeselect(true);
		filterSampStat.addChosenChangeHandler(new ChosenChangeHandler() {
			
			@Override
			public void onChange(ChosenChangeEvent event) {
				passportProxyFilter.setDirty(true);
				Long sampStatId = null;
				try {
					sampStatId = Long.parseLong(event.getValue());
				}
				catch (Exception ex) {}
				if (sampStatId == null) 
					passportProxyFilter.setExpanding(true);
				passportProxyFilter.setSampStatId(sampStatId);
				getUiHandlers().onStartSearch();
			}
		});
		
		filterAlleleAssay.addChosenChangeHandler(new ChosenChangeHandler() {
			
			@Override
			public void onChange(ChosenChangeEvent event) {
				Long alleleAssayId = null;
				try {
					alleleAssayId = Long.parseLong(event.getValue());
				}
				catch (Exception e) {} 
				
				if (alleleAssayId != null) {
					passportProxyFilter.setDirty(true);
					if (event.isSelection()) {
						if (!passportProxyFilter.getAlleleAssayIds().contains(alleleAssayId)) {
							passportProxyFilter.getAlleleAssayIds().add(alleleAssayId);
						}
					}
					else {
						passportProxyFilter.getAlleleAssayIds().remove(alleleAssayId);
						passportProxyFilter.setExpanding(true);
					}
					getUiHandlers().onStartSearch();
				}
			}
		});
		
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				PassportProxy passport = selectionModel.getSelectedObject();
				if (passport == null) {
					resetMarkers();
				}
				else {
					if (passport.getCollection() != null && passport.getCollection().getLocality() != null) {
						LocalityProxy locality = passport.getCollection().getLocality() ;
						LatLng position = LatLng.newInstance(locality.getLatitude(), locality.getLongitude());
						marker.setPosition(position);
						marker.setTitle(passport.getAccename());
						marker.setMap(mapWidget);
					}
				}
			}
		});
		
		marker.addClickHandler(new ClickMapHandler() {

			@Override
			public void onEvent(ClickMapEvent event) {
				drawInfoWindow(marker,event.getMouseEvent());
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	private void initDataGridColumns() {
		PlaceRequest passportDetailRequest = new ParameterizedPlaceRequest(NameTokens.passport);
		IdColumn idColumn = new IdColumn(placeManager,passportDetailRequest);
		passportDataGrid.addColumn(idColumn,"ID");
		passportDataGrid.setColumnWidth(idColumn, 50, Unit.PX);
		
		AccNameColumn nameColumn = new AccNameColumn(passportProxyFilter.getNameSearchTerm());
		passportDataGrid.addColumn(nameColumn,"Name");
		passportDataGrid.setColumnWidth(nameColumn, 150, Unit.PX);
		
		AccNumberColumn accNumbColumn = new AccNumberColumn(passportProxyFilter.getAccNumberSearchTerm());
		passportDataGrid.addColumn(accNumbColumn,"Number");
		passportDataGrid.setColumnWidth(accNumbColumn, 150, Unit.PX);
		
		CollectorColumn collectorColumn = new CollectorColumn(passportProxyFilter.getCollectorSearchTerm());
		passportDataGrid.addColumn(collectorColumn,"Collector");
		passportDataGrid.setColumnWidth(collectorColumn, 200, Unit.PX);
		
		CountryColumn countryColumn = new CountryColumn(flagMap);
		passportDataGrid.addColumn(countryColumn,"Country");
		passportDataGrid.setColumnWidth(countryColumn, 80,Unit.PX);
		
		CollDateColumn collDateColumn = new CollDateColumn();
		passportDataGrid.addColumn(collDateColumn,"Date");
		passportDataGrid.setColumnWidth(collDateColumn, 150,Unit.PX);
		
		TypeColumn typeColumn = new TypeColumn();
		passportDataGrid.addColumn(typeColumn,"Type");
		passportDataGrid.setColumnWidth(typeColumn, 250,Unit.PX);
		
		SourceColumn sourceColumn = new SourceColumn(passportProxyFilter.getSourceSearchTerm());
		passportDataGrid.addColumn(sourceColumn,"Source");
		passportDataGrid.setColumnWidth(sourceColumn, 150,Unit.PX);
		
		AlleleAssayColumn alleleAssayColumn = new AlleleAssayColumn();
		passportDataGrid.addColumn(alleleAssayColumn,"Genotype");
		
	}
	
	@Override
	public HasData<PassportProxy> getPassportDisplay() {
		return passportDataGrid;
	}
	
	@UiHandler({"filterName","filterCollector","filterSource","filterAccNumber","filterCountry"}) 
	public void onFilterKeyPress(KeyPressEvent e) {
		startSearchTimer();
	}
	
	@UiHandler("filterId")
	public void onFilterIdKeyPress(KeyUpEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			Long passportId = null;
			try {
				passportId = Long.parseLong(filterId.getText());
			}
			catch(Exception ex) {}
			if (passportProxyFilter.getPassportId() != passportId)
			{
				if (passportProxyFilter.getPassportId()!= null)
					passportProxyFilter.setExpanding(true);
				passportProxyFilter.setPassportId(passportId);
				updateSearchFilter();
				getUiHandlers().onStartSearch();
			}
		}
	}
	
	@UiHandler({"filterName","filterCollector","filterSource","filterAccNumber","filterCountry"}) 
	public void onFilterSpecialKey(KeyUpEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE || e.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
			passportProxyFilter.setExpanding(true);
			startSearchTimer();
		}
	}
	
	
	
	private void startSearchTimer() {
		if (!isSearchTimerStarted) {
			searchTimer.schedule(SEARCH_INTERVAL);
		}
	}

	@Override
	public void initDataGrid(PassportProxyFilter passportProxyFilter) {
		this.passportProxyFilter = passportProxyFilter; 
		initDataGridColumns();
		
	}
	
	@Override
	public void setCountriesToFilter(Set<String> countries) {
//		filterCountryLb.clear();
//		filterCountryLb.addItem("");
//		for (String country:countries) {
//			filterCountryLb.addItem(country);
//		}
//		filterCountryLb.update();
	}
	

	@Override
	public void setSampstatsToFilter(List<SampStatProxy> sampStats) {
		if (sampStats == null)
			return;
		filterSampStat.clear();
		filterSampStat.addItem("");
		for (SampStatProxy sampstat:sampStats) {
			filterSampStat.addItem(sampstat.getGermplasmType(), sampstat.getId().toString());
		}
		filterSampStat.update();
	}
	
	@Override
	public void setAlleleAssaysToFilter(List<AlleleAssayProxy> alleleAssays) {
		if (alleleAssays == null)
			return;
		filterAlleleAssay.clear();
		filterAlleleAssay.addItem("");
		for (AlleleAssayProxy alleleAssay:alleleAssays) {
			if (alleleAssay != null && alleleAssay.getName() != null)
			   filterAlleleAssay.addItem(alleleAssay.getName(), alleleAssay.getId().toString());
		}
		filterAlleleAssay.update();
		filterAlleleAssay.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
	}
	
	private void updateSearchFilter()  {
		passportProxyFilter.getNameSearchTerm().setValue(filterName.getText());
    	passportProxyFilter.getCollectorSearchTerm().setValue(filterCollector.getText());
    	passportProxyFilter.getCountrySearchTerm().setValue(filterCountry.getText());
    	passportProxyFilter.getSourceSearchTerm().setValue(filterSource.getText());
    	passportProxyFilter.getAccNumberSearchTerm().setValue(filterAccNumber.getText());
    	passportProxyFilter.setDirty(true);
	}
	
	@Override
	public void initMap() {
		if (mapWidget != null)
			return;
		MapOptions opts = MapOptions.newInstance();
		opts.setZoom(1);
		opts.setMapTypeId(MapTypeId.TERRAIN);
		mapWidget = new MapWidget(opts);
		mapWidget.setSize("100%", "100%");
		mapContainer.add(mapWidget);
	}

	@Override
	public void setSelectedAlleleAssayId(Long alleleAssayId,
			boolean startSearch) {
		int index = 0;
		if (alleleAssayId != 0) {
			for (index = 0;index<filterAlleleAssay.getItemCount();index++) {
				if (filterAlleleAssay.getValue(index) != null && filterAlleleAssay.getValue(index).equals(alleleAssayId.toString()))
					break;
			}
		}
		filterAlleleAssay.setSelectedIndex(index);
		if (startSearch) {
			getUiHandlers().onStartSearch();
		}
	}
	
	private void resetMarkers() {
		if (marker == null)
			return;
		marker.close();
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
}
