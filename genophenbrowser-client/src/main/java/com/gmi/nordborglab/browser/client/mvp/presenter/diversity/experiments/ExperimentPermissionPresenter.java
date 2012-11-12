package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.LoadExperimentEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientGinjector;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.PermissionDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class ExperimentPermissionPresenter
		extends
		Presenter<ExperimentPermissionPresenter.MyView, ExperimentPermissionPresenter.MyProxy> {

	public interface MyView extends View {
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.experimentPermission)
	@UseGatekeeper(IsLoggedInGatekeeper.class)
	public interface MyProxy extends TabContentProxyPlace<ExperimentPermissionPresenter> {
	}
	
	 @TabInfo(container = ExperimentDetailTabPresenter.class)
	 static TabData getTabLabel(ClientGinjector ginjector) {
	    // Priority = 1000, means it will be the right-most tab in the home tab
	    return new TabDataDynamic("Permissions", 1000,"",
	        ginjector.getLoggedInGatekeeper());
	 }
	 
	@ContentSlot
	public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();
		
	
	protected final PermissionDetailPresenter permissionDetailPresenter;
	protected final PlaceManager placeManager;
	protected ExperimentProxy experiment;
	protected final ExperimentManager experimentManager;
	protected boolean fireLoadEvent = false;

	@Inject
	public ExperimentPermissionPresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy, 
			final PermissionDetailPresenter permissionDetailPresenter,
			final PlaceManager placeManager,final ExperimentManager experimentManager) {
		super(eventBus, view, proxy);
		this.placeManager = placeManager;
		this.permissionDetailPresenter = permissionDetailPresenter;
		this.experimentManager = experimentManager;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				ExperimentDetailTabPresenter.TYPE_SetTabContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	protected void onReset() {
		super.onReset();
		if (fireLoadEvent) {
			fireEvent(new LoadExperimentEvent(experiment));
			fireLoadEvent = false;
		}
		permissionDetailPresenter.setDomainObject(experiment);
		setInSlot(TYPE_SetMainContent,permissionDetailPresenter);
		//getView().getExperimentDisplayDriver().display(experiment);
		//getView().setState(State.DISPLAYING,getPermission());
		LoadingIndicatorEvent.fire(this, false);
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
	
	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		Receiver<ExperimentProxy> receiver = new Receiver<ExperimentProxy>() {
			@Override
			public void onSuccess(ExperimentProxy exp) {
				experiment = exp;
				fireLoadEvent = true;
				getProxy().manualReveal(ExperimentPermissionPresenter.this);
			}

			@Override
			public void onFailure(ServerFailure error) {
				getProxy().manualRevealFailed();
				placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
			}
		};
		try {
			Long experimentId = Long.valueOf(placeRequest.getParameter("id",
					null));
			if (experiment == null || !experiment.getId().equals(experimentId)) {
				experimentManager.findOne(receiver, experimentId);
			} else {
				getProxy().manualReveal(ExperimentPermissionPresenter.this);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
		}
	}
	
	@ProxyEvent
	public void onLoadExperiment(LoadExperimentEvent event) {
		experiment = event.getExperiment();
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id",experiment.getId().toString());
		String historyToken  = placeManager.buildHistoryToken(request);
		TabDataDynamic tabData = (TabDataDynamic)getProxy().getTabData();
		tabData.setHistoryToken(historyToken);
		boolean canAccess = ((experiment.getUserPermission().getMask() & AccessControlEntryProxy.ADMINISTRATION) ==  AccessControlEntryProxy.ADMINISTRATION);
		tabData.setHasAccess(canAccess);
		if (!canAccess)
			experiment = null;
		getProxy().changeTab(tabData);
	}

}
