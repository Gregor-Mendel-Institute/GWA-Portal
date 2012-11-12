package com.gmi.nordborglab.browser.client.mvp.presenter;

import java.util.List;

import com.gmi.nordborglab.browser.client.mvp.handlers.PermissionUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.PermissionDetailView.PermissionEditDriver;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;

public interface MyView extends View,HasUiHandlers<PermissionUiHandlers> {

	PermissionEditDriver getEditDriver();

	void addPermission(AccessControlEntryProxy permission);

	List<AccessControlEntryProxy> getPermissionList();
}