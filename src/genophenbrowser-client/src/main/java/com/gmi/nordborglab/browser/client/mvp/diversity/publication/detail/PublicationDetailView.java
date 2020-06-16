package com.gmi.nordborglab.browser.client.mvp.diversity.publication.detail;

import com.gmi.nordborglab.browser.client.editors.PublicationDisplayEditor;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiments.ExperimentListDataGridColumns;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;


/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/3/13
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationDetailView extends ViewImpl implements PublicationDetailPresenter.MyView {

    interface Binder extends UiBinder<Widget, PublicationDetailView> {

    }

    public interface PublicationDisplayDriver extends RequestFactoryEditorDriver<PublicationProxy, PublicationDisplayEditor> {
    }

    private final Widget widget;
    private final PlaceManager placeManager;
    protected final PublicationDisplayDriver displayDriver;

    @UiField
    PublicationDisplayEditor publicationDisplayEditor;
    @UiField(provided = true)
    DataGrid<ExperimentProxy> experimentDataGrid;
    @UiField
    CustomPager experimentPager;

    @Inject
    public PublicationDetailView(final Binder binder, final PublicationDisplayDriver displayDriver,
                                 final CustomDataGridResources dataGridResources, final PlaceManager placeManager) {
        this.placeManager = placeManager;
        experimentDataGrid = new DataGrid<ExperimentProxy>(10, dataGridResources, new EntityProxyKeyProvider<ExperimentProxy>());
        experimentDataGrid.setWidth("100%");
        widget = binder.createAndBindUi(this);
        this.displayDriver = displayDriver;
        this.displayDriver.initialize(publicationDisplayEditor);
        experimentPager.setDisplay(experimentDataGrid);
        initExperimentDataGrid();
    }

    private void initExperimentDataGrid() {

        experimentDataGrid.addColumn(new ExperimentListDataGridColumns.NameColumn(placeManager, new PlaceRequest.Builder().nameToken(NameTokens.experiment)), "Name");
        experimentDataGrid.addColumn(new ExperimentListDataGridColumns.DesignColumn(), "Design");
        experimentDataGrid.addColumn(new ExperimentListDataGridColumns.OriginatorColumn(), "Originator");
        experimentDataGrid.addColumn(new ExperimentListDataGridColumns.CommentsColumn(), "Comments");
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public PublicationDisplayDriver getDisplayDriver() {
        return displayDriver;
    }

    @Override
    public HasData<ExperimentProxy> getExperimentDisplay() {
        return experimentDataGrid;
    }
}