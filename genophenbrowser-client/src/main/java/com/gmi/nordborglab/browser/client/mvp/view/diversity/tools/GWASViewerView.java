package com.gmi.nordborglab.browser.client.mvp.view.diversity.tools;

import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.editors.GWASResultEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASViewerUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASViewerPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.EntypoIconActionCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

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

        public enum ACTION {EDIT, DELETE, PERMISSION}

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
    private final PlaceManager placeManager;
    @UiField
    LayoutPanel tabPaneContainer;
    @UiField
    TabPanel tabPanel;
    @UiField
    TabLink gwasListTab;
    @UiField
    SimpleLayoutPanel gwasUploadPanel;
    @UiField
    SimpleLayoutPanel gwasListPanel;
    @UiField
    CustomPager gwasResultPager;
    @UiField(provided = true)
    DataGrid<GWASResultProxy> gwasResultDataGrid;
    @UiField
    SimpleLayoutPanel gwasPlotContainer;
    @UiField
    TabLink gwasUploadTab;

    public enum PANELS {PLOTS, LIST, UPLOAD}

    private PANELS activePanel;
    private boolean isPlotsDisplayed = false;
    private final GWASResultEditDriver editDriver;
    private GWASResultEditEditor gwasEditEditor = new GWASResultEditEditor();
    private Modal editPopUp = new Modal(true);
    private Modal permissionPopUp = new Modal(true);
    private final CurrentUser currentUser;

    @Inject
    public GWASViewerView(final Binder binder, final CustomDataGridResources dataGridResources,
                          final PlaceManager placeManager, final GWASResultEditDriver editDriver,
                          final CurrentUser currentUser) {
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        this.editDriver = editDriver;
        gwasResultDataGrid = new DataGrid<GWASResultProxy>(50, dataGridResources, new EntityProxyKeyProvider<GWASResultProxy>());
        widget = binder.createAndBindUi(this);
        gwasResultPager.setDisplay(gwasResultDataGrid);
        tabPanel.addShowHandler(new TabPanel.ShowEvent.Handler() {
            @Override
            public void onShow(TabPanel.ShowEvent showEvent) {
                if (showEvent.getTarget() == gwasListTab) {
                    activePanel = isPlotsDisplayed ? PANELS.PLOTS : PANELS.LIST;
                } else {
                    activePanel = PANELS.UPLOAD;
                }
                updatePanelVisibility();
            }
        });
        initDataGridColumns();
        this.editDriver.initialize(gwasEditEditor);

        this.gwasEditEditor.getSaveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().saveEdits();
            }
        });

        this.gwasEditEditor.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().cancelEdits();
            }
        });
        editPopUp.setBackdrop(BackdropType.STATIC);
        editPopUp.setTitle("Edit GWAS result");
        editPopUp.add(gwasEditEditor);
        permissionPopUp.setBackdrop(BackdropType.STATIC);
        permissionPopUp.setTitle("Permissions");
        permissionPopUp.setMaxHeigth("700px");
        permissionPopUp.setCloseVisible(false);
        permissionPopUp.setKeyboard(false);
    }

    private void initDataGridColumns() {
        NumberFormat format = NumberFormat.getFormat(NumberFormat.getDecimalFormat().getPattern()).overrideFractionDigits(0);
        final PlaceRequest request = new PlaceRequest(NameTokens.gwasViewer);
        gwasResultDataGrid.addColumn(new HyperlinkPlaceManagerColumn<GWASResultProxy>(new HyperlinkCell(), placeManager) {
            @Override
            public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(GWASResultProxy object) {
                String url = "#" + placeManager.buildHistoryToken(request.with("id", object.getId().toString()));
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
        gwasResultDataGrid.addColumn(new Column<GWASResultProxy, String>(new TextCell()) {
            @Override
            public String getValue(GWASResultProxy object) {
                if (object.getAppUser() == null) {
                    return "N/A";
                }
                if (currentUser.getAppUser().getEmail().equals(object.getAppUser().getEmail())) {
                    return "me";
                } else {
                    return object.getAppUser().getFirstname() + " " + object.getAppUser().getLastname();
                }
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

        gwasResultDataGrid.addColumn(new IdentityColumn<GWASResultProxy>(new CompositeCell<GWASResultProxy>(hasCells)) {
            @Override
            public GWASResultProxy getValue(GWASResultProxy object) {
                return object;
            }
        }, "Actions");

        gwasResultDataGrid.setColumnWidth(0, 70, Style.Unit.PX);
        gwasResultDataGrid.setColumnWidth(4, 100, Style.Unit.PX);
        gwasResultDataGrid.setColumnWidth(5, 100, Style.Unit.PX);
        gwasResultDataGrid.setColumnWidth(6, 120, Style.Unit.PX);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == GWASViewerPresenter.TYPE_SetGWASUploadContent) {
            gwasUploadPanel.setWidget(content);
        } else if (slot == GWASViewerPresenter.TYPE_SetGWASPLOTContent) {
            gwasPlotContainer.setWidget(content);
        } else if (slot == GWASViewerPresenter.TYPE_SetPermissionContent) {
            permissionPopUp.add(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void hideListPanel(boolean hide) {
        if (hide) {
            tabPanel.selectTab(1);
        } else {
            tabPanel.selectTab(0);
        }
        gwasListTab.setVisible(!hide);
    }

    @Override
    public void showPanel(PANELS panel) {
        if (activePanel == panel)
            return;
        activePanel = panel;
        switch (panel) {
            case PLOTS:
            case LIST:
                tabPanel.selectTab(0);
                break;
            case UPLOAD:
                tabPanel.selectTab(1);
                break;
        }
        updatePanelVisibility();
    }

    private void updatePanelVisibility() {
        tabPaneContainer.setWidgetVisible(gwasListPanel, false);
        tabPaneContainer.setWidgetVisible(gwasUploadPanel, false);
        tabPaneContainer.setWidgetVisible(gwasPlotContainer, false);
        switch (activePanel) {
            case PLOTS:
                tabPaneContainer.setWidgetVisible(gwasPlotContainer, true);
                isPlotsDisplayed = true;
                break;
            case LIST:
                tabPaneContainer.setWidgetVisible(gwasListPanel, true);
                isPlotsDisplayed = false;
                break;
            case UPLOAD:
                tabPaneContainer.setWidgetVisible(gwasUploadPanel, true);
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


}