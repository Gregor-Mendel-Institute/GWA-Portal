package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import java.util.Set;

import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.HasUiHandlers;
import javax.validation.ConstraintViolation;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadExperimentEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PlaceRequestEvent.PlaceRequestHandler;
import com.gmi.nordborglab.browser.client.manager.ExperimentManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentDetailView.ExperimentDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments.ExperimentDetailView.ExperimentEditDriver;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;

import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class ExperimentDetailPresenter
		extends
		Presenter<ExperimentDetailPresenter.MyView, ExperimentDetailPresenter.MyProxy>
		implements ExperimentDetailUiHandlers {

	public interface MyView extends View,
			HasUiHandlers<ExperimentDetailUiHandlers> {
		ExperimentEditDriver getExperimentEditDriver();

		ExperimentDisplayDriver getExperimentDisplayDriver();

		void setState(State state,int permission);

		State getState();
	}

	public static enum State {
		DISPLAYING, EDITING, SAVING;
	}

	@ProxyCodeSplit
	@TabInfo(container = ExperimentDetailTabPresenter.class, label = "Overview", priority = 0)
	@NameToken(NameTokens.experiment)
	public interface MyProxy extends
			TabContentProxyPlace<ExperimentDetailPresenter> {
	}

	@TitleFunction
	public String getTitle() {
		String title = null;
		if (experiment != null) {
			title = experiment.getName();
		}
		return title;
	}

	private final PlaceManager placeManager;
	private final ExperimentManager experimentManager;
	ExperimentProxy experiment;
	final CurrentUser currentUser;
	private ExperimentEditDriver editDriver = null;
	private Receiver<ExperimentProxy> receiver = null;
	protected boolean fireLoadExperimentEvent = false;
	
	public static Type<PlaceRequestHandler> type = new Type<PlaceRequestHandler>();
	
	@Inject
	public ExperimentDetailPresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy,
			final PlaceManager placeManager,
			final ExperimentManager experimentManager,
			final CurrentUser currentUser) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		this.placeManager = placeManager;
		this.currentUser = currentUser;
		this.experimentManager = experimentManager;
		this.editDriver = getView().getExperimentEditDriver();
		receiver = new Receiver<ExperimentProxy>() {
			public void onSuccess(ExperimentProxy response) {
				experiment = response;
				getView().setState(State.DISPLAYING,getPermission());
				getView().getExperimentDisplayDriver().display(experiment);
			}

			public void onFailure(ServerFailure error) {
				fireEvent(new DisplayNotificationEvent("Error while saving",error.getMessage(),true,DisplayNotificationEvent.LEVEL_ERROR,0));
				onEdit();
			}

			public void onConstraintViolation(
					Set<ConstraintViolation<?>> violations) {
				super.onConstraintViolation(violations);
				getView().setState(State.EDITING,getPermission());
			}
		};
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
		if (fireLoadExperimentEvent) {
			fireEvent(new LoadExperimentEvent(experiment));
			fireLoadExperimentEvent = false;
		}
		getView().getExperimentDisplayDriver().display(experiment);
		getView().setState(State.DISPLAYING,getPermission());
		LoadingIndicatorEvent.fire(this, false);
	}

	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		Receiver<ExperimentProxy> receiver = new Receiver<ExperimentProxy>() {
			@Override
			public void onSuccess(ExperimentProxy exp) {
				experiment = exp;
				fireLoadExperimentEvent = true;
				getProxy().manualReveal(ExperimentDetailPresenter.this);
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
				getProxy().manualReveal(ExperimentDetailPresenter.this);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
		}
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void onEdit() {
		getView().setState(State.EDITING,getPermission());
		ExperimentRequest ctx = experimentManager.getRequestFactory()
				.experimentRequest();
		editDriver.edit(experiment, ctx);
		ctx.save(experiment).with("userPermission").to(receiver);
	}

	@Override
	public void onSave() {
		getView().setState(State.SAVING,getPermission());
		RequestContext req = editDriver.flush();
		req.fire();
	}

	@Override
	public void onCancel() {
		getView().setState(State.DISPLAYING,getPermission());
		getView().getExperimentDisplayDriver().display(experiment);
	}
	
	protected int getPermission() {
		assert experiment != null;
		int permission = 0;
		if (currentUser.isLoggedIn()) {
			if (experiment.getUserPermission() != null ) {
				permission = experiment.getUserPermission().getMask();
			}
		}
		return permission;
	}
	

	@Override
	public void onDelete() {
		
	}
	
	@ProxyEvent
	public void onLoadExperiment(LoadExperimentEvent event) {
		experiment = event.getExperiment();
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id",experiment.getId().toString());
		String historyToken  = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		getProxy().changeTab(new TabDataDynamic(tabData.getLabel(), tabData.getPriority(), historyToken));
	}
}
