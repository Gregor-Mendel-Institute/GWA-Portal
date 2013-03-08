package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.client.ui.CustomMultiWordSuggestOracle.MultiWordSuggestion;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Set;

public interface PermissionUiHandlers extends UiHandlers {

	public void onSave();

    void onDelete(AccessControlEntryProxy object);

    void onUpdatePermission(AccessControlEntryProxy value);

    void onCancel();

    void onDone();

    void onCancelAddUser();

    void onAddPermission(Set<String> users, AccessControlEntryProxy permission);
}
