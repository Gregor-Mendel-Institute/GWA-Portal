package com.gmi.nordborglab.browser.client.mvp.view.diversity;

import java.util.List;

import com.gmi.nordborglab.browser.client.editors.PermissionEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.PermissionUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.MyView;
import com.gmi.nordborglab.browser.client.oracle.PermissionUserSuggestOracle;
import com.gmi.nordborglab.browser.client.ui.CustomMultiWordSuggestOracle.MultiWordSuggestion;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class PermissionDetailView extends ViewWithUiHandlers<PermissionUiHandlers> implements
		MyView {

	private final Widget widget;
	
	@UiField PermissionEditor permissionEditor;
	@UiField(provided=true) SuggestBox searchBox;
	
	private final PermissionEditDriver permissionEditDriver;

	public interface Binder extends UiBinder<Widget, PermissionDetailView> {
	}
	public interface PermissionEditDriver extends RequestFactoryEditorDriver<CustomAclProxy, PermissionEditor> {}
	

	@Inject
	public PermissionDetailView(final Binder binder, 
			final PermissionEditDriver permissionEditDriver, 
			final CustomRequestFactory rf) {
		searchBox = new SuggestBox(new PermissionUserSuggestOracle(rf));
		widget = binder.createAndBindUi(this);
		searchBox.getElement().setAttribute("placeHolder", "User/Role");
		this.permissionEditDriver = permissionEditDriver;
		this.permissionEditDriver.initialize(permissionEditor);
		
		searchBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(
					SelectionEvent<SuggestOracle.Suggestion> event) {
				getUiHandlers().onAddPermission((MultiWordSuggestion)event.getSelectedItem());
				searchBox.setText("");
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public PermissionEditDriver getEditDriver() {
		return permissionEditDriver;
	}
	
	@UiHandler("save") 
	public void onClick(ClickEvent e) {
		getUiHandlers().onSave();
	}
	
	@Override
	public List<AccessControlEntryProxy> getPermissionList() {
		return permissionEditor.getPermissionList();
	}

	@Override
	public void addPermission(AccessControlEntryProxy permission) {
		permissionEditor.addPermission(permission);
	}
}
