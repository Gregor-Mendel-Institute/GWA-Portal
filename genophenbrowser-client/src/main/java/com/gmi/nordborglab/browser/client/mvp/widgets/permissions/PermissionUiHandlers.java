package com.gmi.nordborglab.browser.client.mvp.widgets.permissions;

import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

public interface PermissionUiHandlers extends UiHandlers {

    void onSave();

    void onSearchUsers(String users, PermissionDetailView.SearchUserCallback callback);

    void onDelete(AccessControlEntryProxy object);

    void onUpdatePermission(AccessControlEntryProxy value);

    void onCancel();

    void onDone();

    void onCancelAddUser();

    void onAddPermission(List<AppUserProxy> users, AccessControlEntryProxy permission);
}
