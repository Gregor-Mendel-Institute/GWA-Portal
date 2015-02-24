package com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list;


import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.AccNameColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.AlleleAssayColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.CollDateColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.CollectorColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.CountryColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.IdColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportDataGridColumns.TypeColumn;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportListPresenter.PassportProxyFilter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class PassportListView extends ViewWithUiHandlers<PassportListViewUiHandlers> implements
        PassportListPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, PassportListView> {
    }

    @UiField(provided = true)
    DataGrid<PassportProxy> passportDataGrid;
    @UiField
    CustomPager pager;
    @UiField
    SimpleLayoutPanel mapContainer;
    @UiField
    LayoutPanel lowerContainer;
    @UiField
    SimplePanel filterContainer;
    private MapWidget mapWidget;
    private static ProvidesKey<PassportProxy> KEY_PROVIDER = new EntityProxyKeyProvider<PassportProxy>();
    private SingleSelectionModel<PassportProxy> selectionModel = new SingleSelectionModel<PassportProxy>(KEY_PROVIDER);

    private final FlagMap flagMap;
    private Marker marker = Marker.newInstance(null);
    private PassportProxyFilter passportProxyFilter;
    private final PlaceManager placeManager;

    @Inject
    public PassportListView(final Binder binder, final FlagMap flagMap,
                            final CustomDataGridResources dataGridResources, final PlaceManager placeManager) {
        this.flagMap = flagMap;
        this.placeManager = placeManager;
        dataGridResources.dataGridStyle().ensureInjected();
        passportDataGrid = new DataGrid<PassportProxy>(10, dataGridResources, KEY_PROVIDER);
        passportDataGrid.setSelectionModel(selectionModel);
        passportDataGrid.setWidth("100%");
        passportDataGrid.setMinimumTableWidth(1280, Unit.PX);
        widget = binder.createAndBindUi(this);
        pager.setDisplay(passportDataGrid);

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                PassportProxy passport = selectionModel.getSelectedObject();
                if (passport == null) {
                    resetMarkers();
                } else {
                    if (passport.getCollection() != null && passport.getCollection().getLocality() != null) {
                        LocalityProxy locality = passport.getCollection().getLocality();
                        LatLng position = LatLng.newInstance(locality.getLatitude(), locality.getLongitude());
                        marker.setPosition(position);
                        marker.setTitle(passport.getAccename() + " (" + passport.getAccename() + ")");
                        marker.setMap(mapWidget);
                    }
                }
            }
        });

        marker.addClickHandler(new ClickMapHandler() {

            @Override
            public void onEvent(ClickMapEvent event) {
                drawInfoWindow(marker, event.getMouseEvent());
            }
        });
        lowerContainer.getElement().getParentElement().getStyle().setOverflow(Overflow.AUTO);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == PassportListPresenter.TYPE_FilterContent) {
            filterContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    private void initDataGridColumns() {
        PlaceRequest.Builder passportDetailRequest = new PlaceRequest.Builder().nameToken(NameTokens.passport);
        IdColumn idColumn = new IdColumn(placeManager, passportDetailRequest);
        passportDataGrid.addColumn(idColumn, "ID");
        passportDataGrid.setColumnWidth(idColumn, 50, Unit.PX);

        AccNameColumn nameColumn = new AccNameColumn(passportProxyFilter.getNameSearchTerm());
        passportDataGrid.addColumn(nameColumn, "Name");
        passportDataGrid.setColumnWidth(nameColumn, 150, Unit.PX);

        CollectorColumn collectorColumn = new CollectorColumn(passportProxyFilter.getCollectorSearchTerm());
        passportDataGrid.addColumn(collectorColumn, "Collector");
        passportDataGrid.setColumnWidth(collectorColumn, 200, Unit.PX);

        CountryColumn countryColumn = new CountryColumn(flagMap);
        passportDataGrid.addColumn(countryColumn, "Country");
        passportDataGrid.setColumnWidth(countryColumn, 65, Unit.PX);

        CollDateColumn collDateColumn = new CollDateColumn();
        passportDataGrid.addColumn(collDateColumn, "Date");
        passportDataGrid.setColumnWidth(collDateColumn, 150, Unit.PX);

        TypeColumn typeColumn = new TypeColumn();
        passportDataGrid.addColumn(typeColumn, "Type");
        passportDataGrid.setColumnWidth(typeColumn, 250, Unit.PX);

        AlleleAssayColumn alleleAssayColumn = new AlleleAssayColumn();
        passportDataGrid.addColumn(alleleAssayColumn, "Genotype");

    }

    @Override
    public HasData<PassportProxy> getPassportDisplay() {
        return passportDataGrid;
    }


    @Override
    public void initDataGrid(PassportProxyFilter passportProxyFilter) {
        this.passportProxyFilter = passportProxyFilter;
        initDataGridColumns();

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
        mapWidget.triggerResize();
    }


    private void resetMarkers() {
        if (marker == null)
            return;
        marker.close();
    }

    private void drawInfoWindow(Marker marker, MouseEvent mouseEvent) {
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
