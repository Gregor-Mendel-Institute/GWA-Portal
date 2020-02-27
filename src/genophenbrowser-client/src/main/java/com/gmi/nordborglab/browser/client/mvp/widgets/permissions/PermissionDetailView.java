package com.gmi.nordborglab.browser.client.mvp.widgets.permissions;


import com.arcbees.chosen.client.ChosenImpl;
import com.arcbees.chosen.client.ChosenOptions;
import com.arcbees.chosen.client.ResultsFilter;
import com.arcbees.chosen.client.gwt.MultipleChosenValueListBox;
import com.gmi.nordborglab.browser.client.editors.PermissionEditor;
import com.gmi.nordborglab.browser.client.ui.cells.PermissionSelectionCell;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionDetailView extends ViewWithUiHandlers<PermissionUiHandlers> implements
        PermissionDetailPresenter.MyView {

    private final Widget widget;

    @UiField(provided = true)
    PermissionEditor permissionEditor;
    @UiField(provided = true)
    MultipleChosenValueListBox<AppUserProxy> usersSearchBox;
    @UiField
    TextBox shareUrlTb;
    @UiField
    DivElement addUserPanel;
    @UiField
    DivElement changeNotificationPanel;
    @UiField
    DivElement donePanel;
    @UiField
    FlowPanel addUserBtnPanel;
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
    private final Map<String, Boolean> selectedUsers = new HashMap<>();

    private final PermissionEditDriver permissionEditDriver;

    public interface Binder extends UiBinder<Widget, PermissionDetailView> {
    }

    public interface PermissionEditDriver extends RequestFactoryEditorDriver<CustomAclProxy, PermissionEditor> {
    }

    static class UserRenderer extends AbstractRenderer<AppUserProxy> {
        @Override
        public String render(AppUserProxy object) {
            return getFullnameFromUser(object);
        }
    }

    public interface SearchUserCallback {
        void onDisplayResults(List<AppUserProxy> users);

    }

    private class UserResultFilter implements ResultsFilter {
        @Override
        public void filter(String searchString, ChosenImpl chosen, boolean isShowing) {
            if (!isShowing)
                return;
            getUiHandlers().onSearchUsers(searchString, users -> {
                List<AppUserProxy> selectedUsers = usersSearchBox.getValue();
                List<AppUserProxy> filteredUsers = Lists.newArrayList(Iterables.filter(users, input -> {
                    for (AppUserProxy us : selectedUsers) {
                        if (us.getId().equals(input.getId())) {
                            return false;
                        }
                    }
                    return true;
                }));
                filteredUsers.addAll(selectedUsers);
                setAvailableUsersToSearch(filteredUsers);
            });
        }
    }


    @Inject
    public PermissionDetailView(final Binder binder,
                                final PermissionEditDriver permissionEditDriver,
                                final PermissionEditor permissionEditor, final PermissionSelectionCell permissionSelectionCell
    ) {
        this.newPermissionDd = new CellWidget<>(permissionSelectionCell);
        this.permissionEditor = permissionEditor;
        ChosenOptions options = new ChosenOptions();
        options.setSingleBackstrokeDelete(true);
        options.setNoResultsText("No user found");
        options.setResultFilter(new UserResultFilter());
        options.setPlaceholderText("Enter name or email address...");
        usersSearchBox = new MultipleChosenValueListBox<>(new UserRenderer(), item -> item.getId(), options);
        usersSearchBox.setWidth("410px");
        widget = binder.createAndBindUi(this);
        this.permissionEditDriver = permissionEditDriver;
        this.permissionEditDriver.initialize(permissionEditor);

        permissionEditor.setDeleteDelegate(object -> getUiHandlers().onDelete(object));
        permissionEditor.setFieldUpdater((index, object, value) -> getUiHandlers().onUpdatePermission(value));
        addUserBtnPanel.setVisible(false);
        changeNotificationPanel.getStyle().setDisplay(Style.Display.NONE);

    }

    private void updateUserSearchPanel() {
        if (usersSearchBox.getValue() == null || usersSearchBox.getValue().size() == 0) {
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
    public void setAvailableUsersToSearch(Collection<AppUserProxy> users) {
        usersSearchBox.setAcceptableValues(users);
    }

    @Override
    public void showModifiedNotificaitonPanel(boolean dirty) {
        changeNotificationPanel.getStyle().setDisplay(dirty ? Style.Display.BLOCK : Style.Display.NONE);
        donePanel.getStyle().setDisplay(dirty ? Style.Display.NONE : Style.Display.BLOCK);
        addUserPanel.getStyle().setDisplay(dirty ? Style.Display.NONE : Style.Display.BLOCK);
    }

    @Override
    public void setAccessControlEntryPlaceHolder(AccessControlEntryProxy newPermission) {
        newPermissionDd.setValue(newPermission, false, true);
        newPermissionDd.redraw();
    }


    @Override
    public void resetUserSearchBox() {
        usersSearchBox.setValue(null);
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
        usersSearchBox.setValue(null);
        selectedUsers.clear();
        updateUserSearchPanel();
    }

    @UiHandler("addUserBtn")
    public void onClickAddUserBtn(ClickEvent e) {
        getUiHandlers().onAddPermission(usersSearchBox.getValue(), newPermissionDd.getValue());
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

    @UiHandler("usersSearchBox")
    public void onChangeUser(ValueChangeEvent<List<AppUserProxy>> e) {
        updateUserSearchPanel();
    }
}
