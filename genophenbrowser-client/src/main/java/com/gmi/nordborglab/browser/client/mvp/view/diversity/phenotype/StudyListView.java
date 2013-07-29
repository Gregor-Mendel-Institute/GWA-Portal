package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyListUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.StudyListPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.AccessColumn;
import com.gmi.nordborglab.browser.client.ui.cells.OwnerColumn;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.List;

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

    @UiField(provided = true)
    DataGrid<StudyProxy> dataGrid;
    @UiField
    CustomPager pager;
    @UiField
    TextBox searchBox;
    @UiField
    NavLink navAll;
    @UiField
    NavLink navPrivate;
    @UiField
    NavLink navPublished;
    @UiField
    NavLink navRecent;
    private final PlaceManager placeManager;
    private final BiMap<ConstEnums.TABLE_FILTER, NavLink> navLinkMap;

    @Inject
    public StudyListView(final Binder binder, final PlaceManager placeManager, final CustomDataGridResources dataGridResources) {
        this.placeManager = placeManager;
        dataGrid = new DataGrid<StudyProxy>(20, dataGridResources, KEY_PROVIDER);
        initCellTable();
        widget = binder.createAndBindUi(this);
        navLinkMap = ImmutableBiMap.<ConstEnums.TABLE_FILTER, NavLink>builder()
                .put(ConstEnums.TABLE_FILTER.ALL, navAll)
                .put(ConstEnums.TABLE_FILTER.PRIVATE, navPrivate)
                .put(ConstEnums.TABLE_FILTER.PUBLISHED, navPublished)
                .put(ConstEnums.TABLE_FILTER.RECENT, navRecent).build();
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
        dataGrid.addColumn(new StudyListDataGridColumns.TitleColumn(placeManager, request), "Title");
        dataGrid.addColumn(new StudyListDataGridColumns.ProtocolColumn(), "Protocol");
        dataGrid.addColumn(new StudyListDataGridColumns.AlleleAssayColumn(), "Genotype");
        dataGrid.addColumn(new StudyListDataGridColumns.StudyDateColumn(), "Study date");
        List<HasCell<StudyJobProxy, ?>> cells = Lists.newArrayList();
        cells.add(new StudyListDataGridColumns.StatusCell());
        cells.add(new StudyListDataGridColumns.ProgressCell());
        dataGrid.addColumn(new StudyListDataGridColumns.StatusColumn(cells), "Status");
        dataGrid.addColumn(new OwnerColumn(), "Owner");
        dataGrid.addColumn(new AccessColumn(), "Access");
        dataGrid.setColumnWidth(0, 50, Style.Unit.PCT);
        dataGrid.setColumnWidth(1, 80, Style.Unit.PX);
        dataGrid.setColumnWidth(2, 50, Style.Unit.PCT);
        dataGrid.setColumnWidth(3, 150, Style.Unit.PX);
        dataGrid.setColumnWidth(4, 200, Style.Unit.PX);
        dataGrid.setColumnWidth(5, 100, Style.Unit.PX);
        dataGrid.setColumnWidth(6, 120, Style.Unit.PX);
    }

    @Override
    public void setActiveNavLink(ConstEnums.TABLE_FILTER filter) {
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
            ConstEnums.TABLE_FILTER type = ConstEnums.TABLE_FILTER.valueOf(facet.getName());
            String newTitle = getFilterTitleFromType(type) + " (" + facet.getTotal() + ")";
            navLinkMap.get(type).setText(newTitle);
        }
    }

    private String getFilterTitleFromType(ConstEnums.TABLE_FILTER filter) {
        switch (filter) {
            case ALL:
                return "All";
            case PRIVATE:
                return "My analyses";
            case PUBLISHED:
                return "Published";
            case RECENT:
                return "Recent";
        }
        return "";
    }

    @UiHandler({"navAll", "navPrivate", "navPublished", "navRecent"})
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

    /*@UiHandler("newStudyBtn")
    public void onNewStudy(ClickEvent e) {
        getUiHandlers().onNewStudy();
    } */

    @Override
    public void showAddBtn(boolean showAdd) {
        // TODO fix this
        //newStudyBtn.setVisible(false);
        //newStudyBtn.setVisible(showAdd);
    }

}
