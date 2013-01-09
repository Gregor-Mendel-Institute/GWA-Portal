package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.PhenotypeListPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class PhenotypeListView extends ViewImpl implements
		PhenotypeListPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, PhenotypeListView> {
	}
	
	
	@UiField(provided=true) DataGrid<PhenotypeProxy> dataGrid;
	@UiField CustomPager pager;
	protected final PlaceManager placeManger;

	@Inject
	public PhenotypeListView(final Binder binder, final PlaceManager placeManger, final CustomDataGridResources dataGridResources) {
		this.placeManger = placeManger;
		dataGrid = new DataGrid<PhenotypeProxy>(20,dataGridResources,new EntityProxyKeyProvider<PhenotypeProxy>());
		initCellTable();
		widget = binder.createAndBindUi(this);
		pager.setDisplay(dataGrid);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	private void initCellTable() {
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.phenotype);
		
		dataGrid.setWidth("100%");
		dataGrid.setEmptyTableWidget(new Label("No Records found"));
		
		dataGrid.addColumn(new PhenotypeListDataGridColumns.NameColumn(placeManger,request),"Name");
		dataGrid.addColumn(new PhenotypeListDataGridColumns.TraitOntologyColumn(),"Trait-Ontology");
		dataGrid.addColumn(new PhenotypeListDataGridColumns.ProtocolColumn(),"Protocol");
		dataGrid.setColumnWidth(0, 15,Unit.PCT);
		dataGrid.setColumnWidth(1, 15,Unit.PCT);
		dataGrid.setColumnWidth(2, 70,Unit.PCT);
	}

	@Override
	public HasData<PhenotypeProxy> getDisplay() {
		return dataGrid;
	}
}
