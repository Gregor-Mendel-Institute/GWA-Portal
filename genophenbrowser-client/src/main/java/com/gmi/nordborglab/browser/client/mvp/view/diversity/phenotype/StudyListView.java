package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyListUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.StudyListPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class StudyListView extends ViewWithUiHandlers<StudyListUiHandlers> implements
		StudyListPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, StudyListView> {
	}
	
	public static ProvidesKey<StudyProxy> KEY_PROVIDER = new ProvidesKey<StudyProxy>() {
		@Override
		public Object getKey(StudyProxy item) {
			if (item != null && item.getId() != null) {
				return item.getId();
			}
			return null;
		}
	};
	
	@UiField(provided=true) DataGrid<StudyProxy> dataGrid;
	@UiField CustomPager pager;
	@UiField Button newStudyBtn;
	
	protected final PlaceManager placeManger;

	@Inject
	public StudyListView(final Binder binder, final PlaceManager placeManager, final CustomDataGridResources dataGridResources) {
		this.placeManger = placeManager;
		dataGrid = new DataGrid<StudyProxy>(20,dataGridResources,KEY_PROVIDER);
		initCellTable();
		widget = binder.createAndBindUi(this);
	    pager.setDisplay(dataGrid);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public HasData<StudyProxy> getDisplay() {
		return dataGrid;
	}
	
	private void initCellTable() {
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.study);
		dataGrid.setEmptyTableWidget(new Label("No Records found"));
		dataGrid.addColumn(new StudyListDataGridColumns.NameColumn(placeManger,request),"Name");
		dataGrid.addColumn(new StudyListDataGridColumns.ProducerColumn(),"Producer");
		dataGrid.addColumn(new StudyListDataGridColumns.ProtocolColumn(),"Protocol");
		dataGrid.addColumn(new StudyListDataGridColumns.AlleleAssayColumn(),"Genotype");
		dataGrid.addColumn(new StudyListDataGridColumns.StudyDateColumn(),"Study date");
	}

	@UiHandler("newStudyBtn")
	public void onNewStudy(ClickEvent e) {
		getUiHandlers().onNewStudy();
	}

	@Override
	public void showAddBtn(boolean showAdd) {
		newStudyBtn.setVisible(showAdd);		
	}
	
}
