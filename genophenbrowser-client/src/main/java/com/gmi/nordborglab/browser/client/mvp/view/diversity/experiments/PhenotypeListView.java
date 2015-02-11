package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;
import com.github.gwtbootstrap.client.ui.event.ShownEvent;
import com.github.gwtbootstrap.client.ui.event.ShownHandler;
import com.gmi.nordborglab.browser.client.mvp.handlers.PhenotypeListViewUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.PhenotypeListPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.widgets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerLinkColumn;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class PhenotypeListView extends ViewWithUiHandlers<PhenotypeListViewUiHandlers> implements
        PhenotypeListPresenter.MyView {

    private final Widget widget;

    public interface Binder extends UiBinder<Widget, PhenotypeListView> {
    }


    @UiField(provided = true)
    DataGrid<PhenotypeProxy> dataGrid;
    @UiField
    CustomPager pager;
    @UiField
    Button uploadPhenotypeBtn;
    @UiField
    SimplePanel facetContainer;


    protected final PlaceManager placeManager;
    private ResizeLayoutPanel phenotypeUploadPanel = new ResizeLayoutPanel();
    private Modal phenotypeUploadPopup = new Modal();

    @Inject
    public PhenotypeListView(final Binder binder, final PlaceManager placeManager, final CustomDataGridResources dataGridResources) {
        this.placeManager = placeManager;
        dataGrid = new DataGrid<PhenotypeProxy>(20, dataGridResources, new EntityProxyKeyProvider<PhenotypeProxy>());
        initCellTable();
        widget = binder.createAndBindUi(this);
        pager.setDisplay(dataGrid);
        phenotypeUploadPopup.add(phenotypeUploadPanel);
        phenotypeUploadPopup.setTitle("Upload phenotype");
        phenotypeUploadPopup.setAnimation(true);
        phenotypeUploadPopup.setBackdrop(BackdropType.STATIC);
        phenotypeUploadPopup.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent hideEvent) {
                getUiHandlers().onClosePhenotypeUploadPopup();
            }
        });

        phenotypeUploadPopup.addShownHandler(new ShownHandler() {
            @Override
            public void onShown(ShownEvent shownEvent) {
                int top = GQuery.$(phenotypeUploadPopup).top();
                int height = Window.getClientHeight() - top;
                phenotypeUploadPopup.setMaxHeigth(height + "px");
                phenotypeUploadPopup.setHeight(height + "px");
                phenotypeUploadPopup.setWidth(Window.getClientWidth() - 50);
                phenotypeUploadPanel.setHeight(GQuery.$(phenotypeUploadPopup).innerHeight() - 50 + "px");
            }
        });
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    private void initCellTable() {
        PlaceRequest request = new PlaceRequest.Builder().nameToken(NameTokens.phenotype).build();

        dataGrid.setWidth("100%");
        dataGrid.setEmptyTableWidget(new Label("No Records found"));

        dataGrid.addColumn(new PhenotypeListDataGridColumns.TitleColumn(placeManager, new PlaceRequest.Builder().nameToken(NameTokens.phenotype)), "Name");
        dataGrid.addColumn(new PhenotypeListDataGridColumns.TraitOntologyColumn(), "Trait-Ontology");
        dataGrid.addColumn(new PhenotypeListDataGridColumns.EnvironOntologyColumn(), "Env-Ontology");
        dataGrid.addColumn(new PhenotypeListDataGridColumns.ProtocolColumn(), "Protocol");
        dataGrid.addColumn(new OwnerLinkColumn(placeManager), "Owner");
        dataGrid.addColumn(new AccessColumn(), "Access");
        dataGrid.setColumnWidth(0, 15, Unit.PCT);
        dataGrid.setColumnWidth(1, 15, Unit.PCT);
        dataGrid.setColumnWidth(2, 15, Unit.PCT);
        dataGrid.setColumnWidth(3, 55, Unit.PCT);
        dataGrid.setColumnWidth(4, 100, Style.Unit.PX);
        dataGrid.setColumnWidth(5, 100, Style.Unit.PX);
    }

    @Override
    public HasData<PhenotypeProxy> getDisplay() {
        return dataGrid;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == PhenotypeListPresenter.TYPE_SetPhenotypeUploadContent) {
            phenotypeUploadPanel.add(content);
        } else if (slot == FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget) {
            facetContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @UiHandler("uploadPhenotypeBtn")
    public void onClickUploadBtn(ClickEvent e) {
        onShowPhenotypeUploadPanel(true);
    }

    @Override
    public void onShowPhenotypeUploadPanel(boolean isShow) {
        if (isShow) {
            phenotypeUploadPopup.show();
        } else {
            phenotypeUploadPopup.hide();
        }
    }

    @Override
    public void showUploadBtn(boolean showAdd) {
        uploadPhenotypeBtn.setVisible(showAdd);
    }
}
