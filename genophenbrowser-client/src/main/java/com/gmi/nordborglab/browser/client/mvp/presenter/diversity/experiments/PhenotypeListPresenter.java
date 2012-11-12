package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadExperimentEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.core.client.Callback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class PhenotypeListPresenter
		extends
		Presenter<PhenotypeListPresenter.MyView, PhenotypeListPresenter.MyProxy> {

	public interface MyView extends View {
		HasData<PhenotypeProxy> getDisplay();
	}
	
	@ProxyCodeSplit
	@TabInfo(container = ExperimentDetailTabPresenter.class, label = "Phenotypes", priority = 1)
	@NameToken(NameTokens.phenotypes)
	public interface MyProxy extends
			TabContentProxyPlace<PhenotypeListPresenter> {
	}

	protected final AsyncDataProvider<PhenotypeProxy> dataProvider;
	private final PhenotypeManager phenotypeManager;
	private ExperimentProxy experiment;
	private final PlaceManager placeManager;
	boolean phenotypesLoaded = false;
	protected Long experimentId = null;
	boolean fireLoadExperimentEvent = false;
	

	@Inject
	public PhenotypeListPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PhenotypeManager phenotypeManager,
			final PlaceManager placeManager) {
		super(eventBus, view, proxy);
		this.phenotypeManager  = phenotypeManager;
		this.placeManager = placeManager;
		dataProvider = new AsyncDataProvider<PhenotypeProxy>() {

			@Override
			protected void onRangeChanged(HasData<PhenotypeProxy> display) {
				requestPhenotypes(null,display.getVisibleRange());
				
			}
		};
	}

	protected void requestPhenotypes(final Callback<Void, Void> callback,final Range range) {
		if (experimentId == null)
			return;
		Receiver<PhenotypePageProxy> receiver = new Receiver<PhenotypePageProxy>() {
			@Override
			public void onSuccess(PhenotypePageProxy phenotypes) {
				dataProvider.updateRowCount(
						(int) phenotypes.getTotalElements(), true);
				dataProvider.updateRowData(range.getStart(), phenotypes.getContent());
				phenotypesLoaded = true;
				if (callback != null)
					callback.onSuccess(null);
			}
			 public void onFailure(ServerFailure error) {
				 fireEvent(new DisplayNotificationEvent("Error", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
				 if (callback != null) {
					 callback.onFailure(null);
				 }
			 }
			
		};
		phenotypeManager.findAll(receiver,experimentId, range.getStart(), range.getLength());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				ExperimentDetailTabPresenter.TYPE_SetTabContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		dataProvider.addDataDisplay(getView().getDisplay());
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		if (fireLoadExperimentEvent) {
			fireEvent(new LoadExperimentEvent(experiment));
			fireLoadExperimentEvent = false;
		}
		LoadingIndicatorEvent.fire(this, false);
		//getProxy().getTab().setTargetHistoryToken(placeManager.buildRelativeHistoryToken(0));
	}

	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		try {
			final Long experimentIdToLoad = Long.valueOf(placeRequest.getParameter("id",null));
			if (!experimentIdToLoad.equals(experimentId)) {
				experimentId = experimentIdToLoad;
				phenotypesLoaded = false;
			}
			if (phenotypesLoaded) {
				getProxy().manualReveal(PhenotypeListPresenter.this);
				return;
			}
			if (experiment == null || !experiment.getId().equals(experimentIdToLoad)) {
				phenotypeManager.requestFactory().experimentRequest().findExperiment(experimentIdToLoad).with("userPermission").fire(new Receiver<ExperimentProxy>() {
	
					@Override
					public void onSuccess(ExperimentProxy response) {
						experiment = response;
						fireLoadExperimentEvent = true;
					}
				});
			}
			requestPhenotypes(new Callback<Void, Void>() {

				@Override
				public void onFailure(Void reason) {
					getProxy().manualRevealFailed();
					placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.experiment).with("id", experimentIdToLoad.toString()));
				}

				@Override
				public void onSuccess(Void result) {
					getProxy().manualReveal(PhenotypeListPresenter.this);
				}
			},getView().getDisplay().getVisibleRange());
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.experiments));
		}
	}
	
	@ProxyEvent
	public void onLoadExperiment(LoadExperimentEvent event) {
		experiment = event.getExperiment();
		if (!experiment.getId().equals(experimentId))
			phenotypesLoaded = false;
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id", experiment.getId().toString());
		String historyToken  = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		getProxy().changeTab(new TabDataDynamic("Phenotypes ("+experiment.getNumberOfPhenotypes()+")", tabData.getPriority(), historyToken));
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
}
