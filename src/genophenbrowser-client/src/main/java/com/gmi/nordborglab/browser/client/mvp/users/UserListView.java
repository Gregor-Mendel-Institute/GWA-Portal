package com.gmi.nordborglab.browser.client.mvp.users;

import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.NumberedPager;
import com.gmi.nordborglab.browser.client.ui.cells.AvatarNameCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.gwtbootstrap3.client.ui.Pagination;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 14:02
 * To change this template use File | Settings | File Templates.
 */
public class UserListView extends ViewWithUiHandlers<UserListUiHandler> implements UserListPresenter.MyView {

    public interface Binder extends UiBinder<Widget, UserListView> {
    }

    @UiField(provided = true)
    DataGrid<AppUserProxy> table;


    private final Widget widget;
    private final PlaceManager placeManager;
    private final AvatarNameCell avatarNameCell;
    private final NumberedPager pager;

    @UiField
    SimplePanel facetContainer;
    @UiField
    Pagination pagination;


    @Inject
    public UserListView(final Binder binder, final PlaceManager placeManager, final CustomDataGridResources dataGridResources,
                        final AvatarNameCell avatarNameCell) {
        this.avatarNameCell = avatarNameCell;
        table = new DataGrid<>(15, dataGridResources);
        widget = binder.createAndBindUi(this);
        bindSlot(FacetSearchPresenterWidget.SLOT_CONTENT, facetContainer);
        initDataGrid();
        pager = new NumberedPager(pagination);
        pager.setDisplay(table);
        this.placeManager = placeManager;
    }

    private void initDataGrid() {
        avatarNameCell.setSize(45);
        table.addColumn(new IdentityColumn<AppUserProxy>(avatarNameCell), "Name");
        table.setMinimumTableWidth(800, Style.Unit.PX);
        table.addColumn(new Column<AppUserProxy, Date>(new DateCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT))) {
            @Override
            public Date getValue(AppUserProxy object) {
                return object.getRegistrationdate();
            }
        }, "Member since");

        table.addColumn(new Column<AppUserProxy, Number>(new NumberCell()) {
            @Override
            public Number getValue(AppUserProxy object) {
                return object.getNumberOfStudies();
            }
        }, "# Studies");
        table.addColumn(new Column<AppUserProxy, Number>(new NumberCell()) {
            @Override
            public Number getValue(AppUserProxy object) {
                return object.getNumberOfPhenotypes();
            }
        }, "# Phenotypes");

        table.addColumn(new Column<AppUserProxy, Number>(new NumberCell()) {
            @Override
            public Number getValue(AppUserProxy object) {
                return object.getNumberOfAnalysis();
            }
        }, "# Analysis");
        table.setColumnWidth(0, 250, Style.Unit.PX);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public HasData<AppUserProxy> getDisplay() {
        return table;
    }


}