package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import java.util.ArrayList;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.PhenotypeListPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.PhenotypeListDataGridColumns.NameColumn;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.PhenotypeListDataGridColumns.ProtocolColumn;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.PhenotypeListDataGridColumns.TraitOntologyColumn;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class PhenotypeListView extends ViewImpl implements
		PhenotypeListPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, PhenotypeListView> {
	}
	
	public static ProvidesKey<PhenotypeProxy> KEY_PROVIDER = new ProvidesKey<PhenotypeProxy>() {
		@Override
		public Object getKey(PhenotypeProxy item) {
			if (item != null && item.getId() != null) {
				return item.getId();
			}
			return null;
		}
	};
	
	@UiField(provided=true) DataGrid<PhenotypeProxy> dataGrid;
	@UiField(provided=true) SimplePager pager;
	protected final PlaceManager placeManger;

	@Inject
	public PhenotypeListView(final Binder binder, final PlaceManager placeManger) {
		this.placeManger = placeManger;
		initCellTable();
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	private void initCellTable() {
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.phenotype);
		dataGrid = new DataGrid<PhenotypeProxy>(20,KEY_PROVIDER);
		dataGrid.setEmptyTableWidget(new Label("No Records found"));
		dataGrid.addColumn(new PhenotypeListDataGridColumns.NameColumn(placeManger,request),"Name");
		dataGrid.addColumn(new PhenotypeListDataGridColumns.ProtocolColumn(),"Protocol");
		dataGrid.addColumn(new PhenotypeListDataGridColumns.TraitOntologyColumn(new ArrayList<String>()),"Trait-Ontology");
	    pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
	    pager.setDisplay(dataGrid);

	}

	@Override
	public HasData<PhenotypeProxy> getDisplay() {
		return dataGrid;
	}
}
