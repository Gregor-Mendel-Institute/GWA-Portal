package com.gmi.nordborglab.browser.client.mvp.view.main;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Pagination;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.gmi.nordborglab.browser.client.mvp.handlers.UserListUiHandler;
import com.gmi.nordborglab.browser.client.mvp.presenter.main.UserListPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.NumberedPager;
import com.gmi.nordborglab.browser.client.ui.cells.AvatarNameCell;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.Date;
import java.util.List;

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


    @UiField
    NavLink navAll;
    @UiField
    NavLink navAdmins;
    @UiField
    NavLink navUsers;
    @UiField
    TextBox searchBox;


    private final Widget widget;
    private final PlaceManager placeManager;
    private final BiMap<ConstEnums.USER_FILTER, NavLink> navLinkMap;
    private final AvatarNameCell avatarNameCell;
    @UiField
    NumberedPager pager;


    @Inject
    public UserListView(final Binder binder, final PlaceManager placeManager, final CustomDataGridResources dataGridResources,
                        final AvatarNameCell avatarNameCell) {
        this.avatarNameCell = avatarNameCell;
        table = new DataGrid<AppUserProxy>(15, dataGridResources);
        widget = binder.createAndBindUi(this);
        navLinkMap = ImmutableBiMap.<ConstEnums.USER_FILTER, NavLink>builder()
                .put(ConstEnums.USER_FILTER.ALL, navAll)
                .put(ConstEnums.USER_FILTER.ADMIN, navAdmins)
                .put(ConstEnums.USER_FILTER.USER, navUsers).build();
        initDataGrid();
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

        table.addColumn(new Column<AppUserProxy, String>(new TextCell()) {
            @Override
            public String getValue(AppUserProxy object) {
                return object.getEmail();
            }
        }, "Email");

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

    @Override
    public void setActiveNavLink(ConstEnums.USER_FILTER filter) {
        for (NavLink link : navLinkMap.values()) {
            link.setActive(false);
        }
        navLinkMap.get(filter).setActive(true);
    }

    @Override
    public void displayFacets(List<FacetProxy> facets) {
        if (facets == null)
            return;
        for (FacetProxy facet : facets) {
            ConstEnums.USER_FILTER type = ConstEnums.USER_FILTER.valueOf(facet.getName());
            String newTitle = getFilterTitleFromType(type) + " (" + facet.getTotal() + ")";
            navLinkMap.get(type).setText(newTitle);
        }
    }

    private String getFilterTitleFromType(ConstEnums.USER_FILTER filter) {
        switch (filter) {
            case ALL:
                return "All";
            case ADMIN:
                return "Admins";
            case USER:
                return "Users";
        }
        return "";
    }

    @UiHandler({"navAll", "navAdmins", "navUsers"})
    public void onNavClick(ClickEvent e) {
        IconAnchor iconAnchor = (IconAnchor) e.getSource();
        getUiHandlers().selectFilter(navLinkMap.inverse().get(iconAnchor.getParent()));
    }

    @UiHandler("searchBox")
    public void onKeyUpSearchBox(KeyUpEvent e) {
        if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER || searchBox.getValue().equalsIgnoreCase("")) {
            getUiHandlers().updateSearchString(searchBox.getValue());
        }
    }
}