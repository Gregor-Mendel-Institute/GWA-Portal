package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;


import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentsOverviewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentsOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentListDataGridColumns.CommentsColumn;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentListDataGridColumns.DesignColumn;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentListDataGridColumns.NameColumn;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentListDataGridColumns.OriginatorColumn;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class ExperimentsOverviewView extends ViewWithUiHandlers<ExperimentsOverviewUiHandlers> implements
		ExperimentsOverviewPresenter.MyView {

	

	public interface Binder extends UiBinder<Widget, ExperimentsOverviewView> {
	}
	
	public static ProvidesKey<ExperimentProxy> KEY_PROVIDER = new ProvidesKey<ExperimentProxy>() {
		@Override
		public Object getKey(ExperimentProxy item) {
			if (item != null && item.getId() != null) {
				return item.getId();
			}
			return null;
		}
	};
	private final Widget widget;
	private final PlaceManager placeManager;
	
	@UiField(provided=true) CellTable<ExperimentProxy> table;

	@Inject
	public ExperimentsOverviewView(final Binder binder,final PlaceManager placeManager) {
		this.placeManager = placeManager;
		initCellTable();
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}


	
	private void initCellTable() {
		FieldUpdater<ExperimentProxy, String> updater = new FieldUpdater<ExperimentProxy, String>() {

			@Override
			public void update(int index, ExperimentProxy object, String value) {
				getUiHandlers().loadExperiment(object);
			}
		};
		table = new CellTable<ExperimentProxy>(15,KEY_PROVIDER);
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
