package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes;


import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
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
import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;

public class PhenotypeListView extends ViewWithUiHandlers<PhenotypeListUiHandlers> implements
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
        dataGrid = new DataGrid<>(20, dataGridResources, new EntityProxyKeyProvider<PhenotypeProxy>());
        initCellTable();
        widget = binder.createAndBindUi(this);

        bindSlot(PhenotypeListPresenter.SLOT_PHENOTYPE_UPLOAD, phenotypeUploadPanel);
        bindSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetContainer);
        pager.setDisplay(dataGrid);
        ModalBody modalBody = new ModalBody();
        modalBody.add(phenotypeUploadPanel);
        phenotypeUploadPopup.add(modalBody);
        phenotypeUploadPopup.setTitle("Upload phenotype");
        phenotypeUploadPopup.addHideHandler(new ModalHideHandler() {
            @Override
            public void onHide(ModalHideEvent modalHideEvent) {
                getUiHandlers().onClosePhenotypeUploadPopup();
            }
        });

        phenotypeUploadPopup.addShownHandler(new ModalShownHandler() {

            @Override
            public void onShown(ModalShownEvent modalShownEvent) {
                int top = GQuery.$(phenotypeUploadPopup).top();
                int height = Window.getClientHeight() - top;
                //phenotypeUploadPopup.setMaxHeigth(height + "px");
                phenotypeUploadPopup.setWidth(Window.getClientWidth() - 50 + "px");
                phenotypeUploadPanel.setHeight(GQuery.$(phenotypeUploadPopup).innerHeight() - 150 + "px");
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
