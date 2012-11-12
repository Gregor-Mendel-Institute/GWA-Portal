package com.gmi.nordborglab.browser.client.mvp.presenter;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.PermissionUiHandlers;
import com.gmi.nordborglab.browser.client.ui.CustomMultiWordSuggestOracle.MultiWordSuggestion;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PermissionRequest;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.PresenterWidget;



public class PermissionDetailPresenter extends
		PresenterWidget<MyView> implements PermissionUiHandlers{

	private EntityProxy proxy; 
	private CustomAclProxy acl;
	private boolean isProxyChanged = false;
	private final CustomRequestFactory rf;
	private Receiver<CustomAclProxy> receiver = null;
	private PermissionRequest editContext = null;

	@Inject
	public PermissionDetailPresenter(final EventBus eventBus, final MyView view, final CustomRequestFactory rf) {
		super(eventBus, view);
		getView().setUiHandlers(this);
		this.rf = rf;
		receiver = new Receiver<CustomAclProxy>() {

			public void onSuccess(CustomAclProxy response) {
				fireEvent(new LoadingIndicatorEvent(false));
				acl = response;
				onEdit();
			}

			public void onFailure(ServerFailure error) {
				fireEvent(new LoadingIndicatorEvent(false));
				fireEvent(new DisplayNotificationEvent("Error while saving",error.getMessage(),true,DisplayNotificationEvent.LEVEL_ERROR,0));
				onEdit();
			}

			public void onConstraintViolation(
					Set<ConstraintViolation<?>> violations) {
				super.onConstraintViolation(violations);
				fireEvent(new LoadingIndicatorEvent(false));
				//getView().setState(State.EDITING,getPermission());
			}
		};
	}

	@Override
	protected void onBind() {
		super.onBind();
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		if (isProxyChanged) {
			rf.permissionRequest().getPermissions((ExperimentProxy)proxy).fire(new Receiver<CustomAclProxy>() {

				@Override
				public void onSuccess(CustomAclProxy response) {
					acl = response;
					onEdit();
				}
			});
		}
	}
	
	public void setDomainObject(EntityProxy proxy) {
		isProxyChanged = !proxy.equals(this.proxy);
		this.proxy = proxy;
	}

	@Override
	public void onSave() {
		PermissionRequest ctx = (PermissionRequest)getView().getEditDriver().flush();
		ctx.fire();
		fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
	}
	
	public void onEdit() {
		editContext = rf.permissionRequest();
		editContext.updatePermissions((ExperimentProxy)proxy, acl).to(receiver);
		getView().getEditDriver().edit(acl,editContext);
	}

	@Override
	public void onAddPermission(MultiWordSuggestion selectedItem) {
		boolean isAdd = true;
		List<AccessControlEntryProxy> permissionList = getView().getPermissionList();
		for (AccessControlEntryProxy permission:permissionList) {
			if (permission.getPrincipal().getId().equals(selectedItem.getPrincipal().getId())) {
				isAdd = false;
				break;
			}
		}
		if (isAdd) {
			AccessControlEntryProxy newPermission = editContext.create(AccessControlEntryProxy.class);
			newPermission.setIsGranting(true);
			newPermission.setPrincipal(selectedItem.getPrincipal());
			permissionList.add(newPermission);
		}
	}
}
