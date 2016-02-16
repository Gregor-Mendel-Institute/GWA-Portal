package com.gmi.nordborglab.browser.client.mvp.diversity.experiments;


import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.AvatarNameCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class ExperimentsOverviewView extends ViewWithUiHandlers<ExperimentsOverviewUiHandlers> implements
        ExperimentsOverviewPresenter.MyView {


    public interface Binder extends UiBinder<Widget, ExperimentsOverviewView> {
    }

    private final Widget widget;
    private final PlaceManager placeManager;
    private final AvatarNameCell avatarNameCell;

    @UiField(provided = true)
    DataGrid<ExperimentProxy> table;
    @UiField
    CustomPager pager;
    @UiField
    SimplePanel facetContainer;


    @Inject
    public ExperimentsOverviewView(final Binder binder,
                                   final PlaceManager placeManager, final CustomDataGridResources dataGridResources,
                                   final AvatarNameCell avatarNameCell) {
        this.placeManager = placeManager;
        this.avatarNameCell = avatarNameCell;
        table = new DataGrid<ExperimentProxy>(50, dataGridResources, new EntityProxyKeyProvider<ExperimentProxy>());
        initCellTable();
        widget = binder.createAndBindUi(this);
        bindSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetContainer);
        pager.setDisplay(table);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    private void initCellTable() {

        table.addColumn(new ExperimentListDataGridColumns.TitleColumn(placeManager, new PlaceRequest.Builder().nameToken(NameTokens.experiment)), "Name");
        table.addColumn(new ExperimentListDataGridColumns.DesignColumn(), "Design");
        /*table.addColumn(new OwnerLinkColumn(placeManager), "Owner");*/
        table.addColumn(new Column<ExperimentProxy, AppUserProxy>(avatarNameCell) {
            @Override
            public AppUserProxy getValue(ExperimentProxy object) {
                return object.getOwnerUser();
            }
        }, "Owner");
        table.addColumn(new AccessColumn(), "Access");

        table.setColumnWidth(2, 250, Style.Unit.PX);
        table.setColumnWidth(3, 150, Style.Unit.PX);

    }

    @Override
    public HasData<ExperimentProxy> getDisplay() {
        return table;
    }
}
