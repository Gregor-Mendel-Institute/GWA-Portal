package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;



import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentsOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentsOverviewPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class ExperimentsOverviewView extends ViewWithUiHandlers<ExperimentsOverviewUiHandlers> implements
		ExperimentsOverviewPresenter.MyView {

	

	public interface Binder extends UiBinder<Widget, ExperimentsOverviewView> {
	}
	
	private final Widget widget;
	private final PlaceManager placeManager;
	
	@UiField(provided=true) DataGrid<ExperimentProxy> table;
	@UiField CustomPager pager;

	@Inject
	public ExperimentsOverviewView(final Binder binder,
			final PlaceManager placeManager, final CustomDataGridResources dataGridResources) {
		this.placeManager = placeManager;
		table = new DataGrid<ExperimentProxy>(50,dataGridResources,new EntityProxyKeyProvider<ExperimentProxy>());
		initCellTable();
		widget = binder.createAndBindUi(this);
		pager.setDisplay(table);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}


	
	private void initCellTable() {
		
		table.addColumn(new ExperimentListDataGridColumns.NameColumn(placeManager,new ParameterizedPlaceRequest(NameTokens.experiment)),"Name");
		table.addColumn(new ExperimentListDataGridColumns.DesignColumn(),"Design");
		table.addColumn(new ExperimentListDataGridColumns.OriginatorColumn(),"Originator");
		table.addColumn(new ExperimentListDataGridColumns.CommentsColumn(),"Comments");
	}

	@Override
	public HasData<ExperimentProxy> getDisplay() {
		return table;
	}
}
