package com.gmi.nordborglab.browser.client.mvp.diversity.tools.gwasviewer;


import com.gmi.nordborglab.browser.client.editors.GWASResultEditEditor;
import com.gmi.nordborglab.browser.client.mvp.widgets.facets.FacetSearchPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.PlotDownloadPopup;
import com.gmi.nordborglab.browser.client.ui.cells.AvatarNameCell;
import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASViewerView extends ViewWithUiHandlers<GWASViewerUiHandlers> implements GWASViewerPresenter.MyView {

    interface Binder extends UiBinder<Widget, GWASViewerView> {

    }

    public interface GWASResultEditDriver extends RequestFactoryEditorDriver<GWASResultProxy, GWASResultEditEditor> {

    }

    public static class ActionHasCell implements HasCell<GWASResultProxy, GWASResultProxy> {


        public enum ACTION {EDIT, DELETE, PERMISSION;}

        private final Cell<GWASResultProxy> cell;

        private ActionHasCell(final Cell<GWASResultProxy> cell) {
            this.cell = cell;
        }

        @Override
        public Cell<GWASResultProxy> getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<GWASResultProxy, GWASResultProxy> getFieldUpdater() {
            return null;
        }

        @Override
        public GWASResultProxy getValue(GWASResultProxy object) {
            return object;
        }

    }

    private final Widget widget;
    private final AvatarNameCell avatarNameCell;
    private final PlaceManager placeManager;
    private final Modal plotPoupup = new Modal();
    private final PlotDownloadPopup plotDownload = new PlotDownloadPopup(PlotDownloadPopup.PLOT_TYPE.GWASVIEWER);
    @UiField
    DeckLayoutPanel tabPaneContainer;
    @UiField
    NavTabs tabPanel;
    @UiField
    TabListItem gwasListTab;
    @UiField
    SimpleLayoutPanel gwasUploadPanel;
    @UiField
    SimpleLayoutPanel gwasListPanel;
    @UiField
    CustomPager gwasResultPager;
    @UiField(provided = true)
    DataGrid<GWASResultProxy> gwasResultDataGrid;
    @UiField
    LayoutPanel gwasPlotContainer;
    @UiField
    TabListItem gwasUploadTab;
    @UiField
    SimplePanel facetContainer;
    @UiField
    SimpleLayoutPanel gwasPlots;

    private final class DeleteCallBack implements AlertCallback {

        private GWASResultProxy object;

        public void setObject(GWASResultProxy object) {
            this.object = object;
        }

        @Override
        public void callback() {
            getUiHandlers().onConfirmDelete(object);
        }
    }

    private final DeleteCallBack deleteCallBack = new DeleteCallBack();


    public enum PANELS {PLOTS, LIST, UPLOAD}

    private PANELS activePanel;
    private boolean isPlotsDisplayed = false;
    private final GWASResultEditDriver editDriver;
    private GWASResultEditEditor gwasEditEditor = new GWASResultEditEditor();
    private Modal editPopUp = new Modal();
    private Modal permissionPopUp = new Modal();
    private Bootbox.Dialog deletePopup = Bootbox.Dialog.create();
    private final CurrentUser currentUser;

    @Inject
    public GWASViewerView(final Binder binder, final CustomDataGridResources dataGridResources,
                          final PlaceManager placeManager, final GWASResultEditDriver editDriver,
                          final CurrentUser currentUser,
                          final AvatarNameCell avatarNameCell) {
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        this.editDriver = editDriver;
        this.avatarNameCell = avatarNameCell;
        gwasResultDataGrid = new DataGrid<>(50, dataGridResources, new EntityProxyKeyProvider<GWASResultProxy>());
        widget = binder.createAndBindUi(this);
        tabPaneContainer.showWidget(0);
        gwasResultPager.setDisplay(gwasResultDataGrid);
        initDataGridColumns();
        this.editDriver.initialize(gwasEditEditor);

        Button saveBtn = new Button("Save", IconType.SAVE, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().saveEdits();
            }
        });
        saveBtn.setType(ButtonType.PRIMARY);

        Button cancelBtn = new Button("Cancel", IconType.SAVE, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().cancelEdits();
            }
        });
        cancelBtn.setType(ButtonType.DEFAULT);
        ModalFooter footer = new ModalFooter();
        footer.add(saveBtn);
        footer.add(cancelBtn);

        ModalBody modalBody = new ModalBody();
        modalBody.add(gwasEditEditor);
        editPopUp.setTitle("Edit GWAS result");
        editPopUp.add(modalBody);
        editPopUp.add(footer);

        permissionPopUp.setDataBackdrop(ModalBackdrop.STATIC);
        permissionPopUp.setTitle("Permissions");
        permissionPopUp.setClosable(false);
        permissionPopUp.setDataKeyboard(false);

        modalBody = new ModalBody();
        modalBody.add(plotDownload);
        plotPoupup.setTitle("Download plots");
        plotPoupup.add(modalBody);
        // FIXME change once we have information about availabilty of macs
        plotDownload.setMacFilterEnabled(false);

        deletePopup.setTitle("Delete GWAS result");
        deletePopup.addButton("Cancel");
        deletePopup.addButton("Delete", ButtonType.DANGER.getCssName(), deleteCallBack);
    }

    private void initDataGridColumns() {
        NumberFormat format = NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0);
        final PlaceRequest.Builder request = new PlaceRequest.Builder().nameToken(NameTokens.gwasViewer);
        gwasResultDataGrid.addColumn(new HyperlinkPlaceManagerColumn<GWASResultProxy>(new HyperlinkCell(), placeManager) {
            @Override
            public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(GWASResultProxy object) {
                String url = "#" + placeManager.buildHistoryToken(request.with("id", object.getId().toString()).build());
                return new HyperlinkPlaceManagerColumn.HyperlinkParam(object.getId().toString(), url);
            }
        }, "ID");
        gwasResultDataGrid.addColumn(new Column<GWASResultProxy, String>(new TextCell()) {
            @Override
            public String getValue(GWASResultProxy object) {
                return object.getName();
            }
        }, "Name");

        gwasResultDataGrid.addColumn(new Column<GWASResultProxy, String>(new TextCell()) {
            @Override
            public String getValue(GWASResultProxy object) {
                return object.getType();
            }
        }, "Type");
        gwasResultDataGrid.addColumn(new Column<GWASResultProxy, AppUserProxy>(avatarNameCell) {
            @Override
            public AppUserProxy getValue(GWASResultProxy object) {
                return object.getOwnerUser();
            }
        }, "Owner");


        gwasResultDataGrid.addColumn(new Column<GWASResultProxy, Number>(new NumberCell(format)) {
            @Override
            public Number getValue(GWASResultProxy object) {
                return object.getNumberOfSNPs();
            }
        }, "#SNPs");
        gwasResultDataGrid.addColumn(new Column<GWASResultProxy, Number>(new NumberCell()) {
            @Override
            public Number getValue(GWASResultProxy object) {
                return object.getMaxScore();
            }
        }, "Max Score");

        List<HasCell<GWASResultProxy, ?>> hasCells = Lists.newArrayList();
        hasCells.add(new ActionHasCell(new EntypoIconActionCell<GWASResultProxy>("e_icon-pencil", new ActionCell.Delegate<GWASResultProxy>() {
            @Override
            public void execute(GWASResultProxy object) {
                getUiHandlers().onEdit(object);
            }
        }, true)));

        hasCells.add(new ActionHasCell(new EntypoIconActionCell<GWASResultProxy>("e_icon-key", new ActionCell.Delegate<GWASResultProxy>() {
            @Override
            public void execute(GWASResultProxy object) {
                getUiHandlers().onShowPermissions(object);
            }
        }, true)));

        hasCells.add(new ActionHasCell(new EntypoIconActionCell<GWASResultProxy>("e_icon-trash", new ActionCell.Delegate<GWASResultProxy>() {
            @Override
            public void execute(GWASResultProxy object) {
                getUiHandlers().onDelete(object);
            }
        }, true)));

        hasCells.add(new ActionHasCell(new EntypoIconActionCell<GWASResultProxy>("e_icon-picture", new ActionCell.Delegate<GWASResultProxy>() {
            @Override
            public void execute(GWASResultProxy object) {
                plotDownload.setId(object.getId());
                showPlotDownloadPopup();
            }
        }, true)));

        gwasResultDataGrid.addColumn(new IdentityColumn<GWASResultProxy>(new CompositeCell<GWASResultProxy>(hasCells)) {
            @Override
            public GWASResultProxy getValue(GWASResultProxy object) {
                return object;
            }
        }, "Actions");

        gwasResultDataGrid.setColumnWidth(0, 70, Style.Unit.PX);
        gwasResultDataGrid.setColumnWidth(4, 100, Style.Unit.PX);
        gwasResultDataGrid.setColumnWidth(5, 100, Style.Unit.PX);
        gwasResultDataGrid.setColumnWidth(6, 160, Style.Unit.PX);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {

        if (slot == GWASViewerPresenter.TYPE_SetGWASUploadContent) {
            gwasUploadPanel.setWidget(content);
        } else if (slot == GWASViewerPresenter.TYPE_SetGWASPLOTContent) {
            gwasPlots.setWidget(content);
        } else if (slot == GWASViewerPresenter.TYPE_SetPermissionContent) {
            ModalBody modalBody = new ModalBody();
            modalBody.add(content);
            permissionPopUp.add(modalBody);
        } else if (slot == FacetSearchPresenterWidget.TYPE_SetFacetSearchWidget) {
            facetContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }


    @Override
    public void showPanel(PANELS panel) {
        if (activePanel == panel)
            return;
        activePanel = panel;
        switch (panel) {
            case PLOTS:
            case LIST:
                gwasListTab.showTab(false);
                break;
            case UPLOAD:
                gwasUploadTab.showTab(false);
                break;
        }
        updatePanelVisibility();
    }

    private void updatePanelVisibility() {
        gwasListTab.setActive(false);
        gwasUploadTab.setActive(false);
        switch (activePanel) {
            case PLOTS:
                isPlotsDisplayed = true;
                tabPaneContainer.showWidget(gwasPlotContainer);
                gwasListTab.setActive(true);
                break;
            case LIST:
                tabPaneContainer.showWidget(gwasListPanel);
                gwasListTab.setActive(true);
                isPlotsDisplayed = false;
                break;
            case UPLOAD:
                tabPaneContainer.showWidget(gwasUploadPanel);
                gwasUploadTab.setActive(true);
                break;
        }
    }


    @Override
    public HasData<GWASResultProxy> getDisplay() {
        return gwasResultDataGrid;
    }

    @Override
    public void hideUploadPanel(boolean hide) {
        gwasUploadTab.setVisible(!hide);
    }


    @Override
    public GWASResultEditDriver getEditDriver() {
        return editDriver;
    }

    @Override
    public void showEditPanel(boolean show) {
        if (show)
            editPopUp.show();
        else
            editPopUp.hide();

    }

    @Override
    public void showPermissionPanel(boolean show) {
        if (show)
            permissionPopUp.show();
        else
            permissionPopUp.hide();

    }

    @Override
    public void setGWAResultId(Long id) {
        plotDownload.setId(id);
    }

    @Override
    public void showDeletePopup(GWASResultProxy object) {
        deleteCallBack.setObject(object);
        deletePopup.setMessage("Do you really want to delete " + object.getName());
        Bootbox.dialog(deletePopup);
    }

    private void showPlotDownloadPopup() {
        plotPoupup.show();
    }

    @UiHandler("downloadBtn")
    public void onClickDownloadBtn(ClickEvent e) {
        showPlotDownloadPopup();
    }

    @UiHandler("gwasUploadTab")
    public void onSelectUploadTab(TabShowEvent e) {
        activePanel = PANELS.UPLOAD;
        updatePanelVisibility();
    }

    @UiHandler("gwasListTab")
    public void onSelectListTab(TabShowEvent e) {
        activePanel = isPlotsDisplayed ? PANELS.PLOTS : PANELS.LIST;
        updatePanelVisibility();
    }
}