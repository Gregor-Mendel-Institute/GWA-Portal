package com.gmi.nordborglab.browser.client.mvp.widgets.permissions;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonToolbar;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.gmi.nordborglab.browser.client.editors.PermissionEditor;
import com.gmi.nordborglab.browser.client.ui.cells.PermissionSelectionCell;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.watopi.chosen.client.ChosenOptions;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.gwt.ChosenListBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionDetailView extends ViewWithUiHandlers<PermissionUiHandlers> implements
        PermissionDetailPresenter.MyView {

    private final Widget widget;

    @UiField(provided = true)
    PermissionEditor permissionEditor;
    @UiField(provided = true)
    ChosenListBox usersSearchBox;
    @UiField
    TextBox shareUrlTb;
    @UiField
    FluidRow addUserPanel;
    @UiField
    FluidRow changeNotificationPanel;
    @UiField
    FluidRow donePanel;
    @UiField
    ButtonToolbar addUserBtnPanel;
    @UiField
    Button addUserBtn;
    @UiField
    Button cancelUserBtn;
    @UiField
    Button saveBtn;
    @UiField
    Button cancelBtn;
    @UiField
    Button doneBtn;
    @UiField(provided = true)
    CellWidget<AccessControlEntryProxy> newPermissionDd;
    private final Map<String, Boolean> selectedUsers = new HashMap<String, Boolean>();

    private final PermissionEditDriver permissionEditDriver;

    public interface Binder extends UiBinder<Widget, PermissionDetailView> {
    }

    public interface PermissionEditDriver extends RequestFactoryEditorDriver<CustomAclProxy, PermissionEditor> {
    }


    @Inject
    public PermissionDetailView(final Binder binder,
                                final PermissionEditDriver permissionEditDriver,
                                final PermissionEditor permissionEditor, final PermissionSelectionCell permissionSelectionCell
    ) {
        this.newPermissionDd = new CellWidget<AccessControlEntryProxy>(permissionSelectionCell);
        this.permissionEditor = permissionEditor;
        ChosenOptions options = new ChosenOptions();
        options.setSingleBackstrokeDelete(true);
        options.setNoResultsText("No user found");
        usersSearchBox = new ChosenListBox(true, options);
        widget = binder.createAndBindUi(this);
        this.permissionEditDriver = permissionEditDriver;
        this.permissionEditDriver.initialize(permissionEditor);
        usersSearchBox.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
            @Override
            public void onChange(ChosenChangeEvent chosenChangeEvent) {
                if (chosenChangeEvent.isSelection()) {
                    selectedUsers.put(chosenChangeEvent.getValue(), true);
                } else {
                    selectedUsers.remove(chosenChangeEvent.getValue());
                }
                updateUserSearchPanel();
            }
        });

        permissionEditor.setDeleteDelegate(new ActionCell.Delegate<AccessControlEntryProxy>() {
            @Override
            public void execute(AccessControlEntryProxy object) {
                getUiHandlers().onDelete(object);
            }
        });
        permissionEditor.setFieldUpdater(new FieldUpdater<AccessControlEntryProxy, AccessControlEntryProxy>() {
            @Override
            public void update(int index, AccessControlEntryProxy object, AccessControlEntryProxy value) {
                getUiHandlers().onUpdatePermission(value);
            }
        });
        addUserBtnPanel.setVisible(false);
        changeNotificationPanel.setVisible(false);

    }

    private void updateUserSearchPanel() {
        if (selectedUsers.size() == 0) {
            addUserBtnPanel.setVisible(false);
            newPermissionDd.setVisible(false);
        } else {
            newPermissionDd.setVisible(true);
            addUserBtnPanel.setVisible(true);
        }
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public PermissionEditDriver getEditDriver() {
        return permissionEditDriver;
    }


    @Override
    public List<AccessControlEntryProxy> getPermissionList() {
        return permissionEditor.getPermissionList();
    }

    @Override
    public void setShareUrl(String shareUrl) {
        shareUrlTb.setText(shareUrl);
    }

    @Override
    public String getModuleUrl() {
        return GWT.getHostPageBaseURL();
    }

    @Override
    public void setAvailableUsersToSearch(List<AppUserProxy> users) {
        usersSearchBox.clear();
        for (AppUserProxy user : users) {
            usersSearchBox.addItem(getFullnameFromUser(user), user.getId().toString());
        }
    }

    @Override
    public void showModifiedNotificaitonPanel(boolean dirty) {
        changeNotificationPanel.setVisible(dirty);
        donePanel.setVisible(!dirty);
        addUserPanel.setVisible(!dirty);
    }

    @Override
    public void setAccessControlEntryPlaceHolder(AccessControlEntryProxy newPermission) {
        newPermissionDd.setValue(newPermission, false, true);
        newPermissionDd.redraw();
    }

    @Override
    public void resetUserSearchBox() {
        usersSearchBox.setSelectedIndex(-1);
        selectedUsers.clear();
        updateUserSearchPanel();
    }


    private static String getFullnameFromUser(AppUserProxy user) {
        return user.getFirstname() + " " + user.getLastname() + " (" + user.getEmail() + ")";
    }

    @Override
    public void addPermission(AccessControlEntryProxy permission) {
        permissionEditor.addPermission(permission);
    }


    @UiHandler("cancelUserBtn")
    public void onClickCancelAddUserBtn(ClickEvent e) {
        getUiHandlers().onCancelAddUser();
        usersSearchBox.setSelectedIndex(-1);
        selectedUsers.clear();
        updateUserSearchPanel();

    }


    @UiHandler("addUserBtn")
    public void onClickAddUserBtn(ClickEvent e) {
        getUiHandlers().onAddPermission(selectedUsers.keySet(), newPermissionDd.getValue());
    }

    @UiHandler("cancelBtn")
    public void onCancelBtn(ClickEvent e) {
        getUiHandlers().onCancel();
    }

    @UiHandler("doneBtn")
    public void onClickDoneBtn(ClickEvent e) {
        getUiHandlers().onDone();
    }

    @UiHandler("saveBtn")
    public void onClickSave(ClickEvent e) {
        getUiHandlers().onSave();
    }
}
