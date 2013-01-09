package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyOverviewPresenter;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype.StudyListDataGridColumns;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
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

public class StudyOverviewView extends ViewImpl implements
		StudyOverviewPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, StudyOverviewView> {
	}
	
	@UiField(provided = true)
	DataGrid<StudyProxy> dataGrid;
	@UiField
	CustomPager pager;
	private final PlaceManager placeManger;

	@Inject
	public StudyOverviewView(final Binder binder,final PlaceManager placeManger,
			final CustomDataGridResources dataGridResources) {
		this.placeManger = placeManger;
		dataGrid = new DataGrid<StudyProxy>(20,dataGridResources, new EntityProxyKeyProvider<StudyProxy>());
		initGrid();
		widget = binder.createAndBindUi(this);
		pager.setDisplay(dataGrid);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	private void initGrid() {
		PlaceRequest request = new ParameterizedPlaceRequest(
				NameTokens.study);
		dataGrid.setWidth("100%");
		dataGrid.setEmptyTableWidget(new Label("No Records found"));
		
		dataGrid.setEmptyTableWidget(new Label("No Records found"));
		dataGrid.addColumn(new StudyListDataGridColumns.NameColumn(placeManger,request),"Name");
		dataGrid.addColumn(new StudyListDataGridColumns.ExperimentColumn(),"Experiment");
		dataGrid.addColumn(new StudyListDataGridColumns.PhenotypeColumn(),"Phenotype");
		dataGrid.addColumn(new StudyListDataGridColumns.ProtocolColumn(),"Protocol");
		dataGrid.addColumn(new StudyListDataGridColumns.AlleleAssayColumn(),"Genotype");
		dataGrid.addColumn(new StudyListDataGridColumns.StudyDateColumn(),"Study date");
		dataGrid.setColumnWidth(0, 15, Unit.PCT);
		dataGrid.setColumnWidth(1, 15, Unit.PCT);
		dataGrid.setColumnWidth(2, 15, Unit.PCT);
		dataGrid.setColumnWidth(3, 55, Unit.PX);
		dataGrid.setColumnWidth(4, 150, Unit.PX);
		dataGrid.setColumnWidth(5, 80, Unit.PX);
		
	}
	
	@Override
	public HasData<StudyProxy> getDisplay() {
		return dataGrid;
	}
}
