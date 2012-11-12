package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.dispatch.CustomCallback;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataActionResult;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.PhenotypeListPresenter;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.dispatch.shared.DispatchAsync;
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

public class StudyGWASPlotPresenter
		extends
		Presenter<StudyGWASPlotPresenter.MyView, StudyGWASPlotPresenter.MyProxy> {

	public interface MyView extends View {

		void drawGWASPlots(GWASDataDTO gwasData);
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.studygwas)
	@TabInfo(label="GWAS-Plots",priority=1,container=StudyTabPresenter.class)
	public interface MyProxy extends TabContentProxyPlace<StudyGWASPlotPresenter> {
	}
	
	protected StudyProxy study;
	protected Long studyId;
	protected boolean gwasPlotsLoaded = false; 
	protected final PlaceManager placeManager;
	protected boolean fireLoadEvent = false;
	protected final DispatchAsync dispatch;
	protected final CdvManager cdvManager;

	@Inject
	public StudyGWASPlotPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PlaceManager placeManager, 
			final DispatchAsync dispatch, final CdvManager cdvManager) {
		super(eventBus, view, proxy);
		this.dispatch = dispatch;
		this.placeManager = placeManager;
		this.cdvManager = cdvManager;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, StudyTabPresenter.TYPE_SetTabContent, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	protected void onReset() {
		super.onReset();
		if (fireLoadEvent) {
			fireEvent(new LoadStudyEvent(study));
			fireLoadEvent = false;
		}
		dispatch.execute(new GetGWASDataAction(studyId), new CustomCallback<GetGWASDataActionResult>(getEventBus()) {
			
			@Override
			public void onSuccess(GetGWASDataActionResult result) {
				getView().drawGWASPlots(result.getResultData());
				LoadingIndicatorEvent.fire(this, false);
			}
		});
	}
	
	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		try {
			final Long studyIdToLoad = Long.valueOf(placeRequest.getParameter("id",null));
			if (!studyIdToLoad.equals(studyId)) {
				studyId = studyIdToLoad;
				gwasPlotsLoaded = false;
			}
			if (gwasPlotsLoaded) {
				getProxy().manualReveal(StudyGWASPlotPresenter.this);
				return;
			}
			if (study == null || !study.getId().equals(studyIdToLoad)) {
				cdvManager.findOne(new Receiver<StudyProxy>() {
	
					@Override
					public void onSuccess(StudyProxy response) {
						study = response;
						fireLoadEvent = true;
					}
				},studyIdToLoad);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.experiments));
		}
	}
	
	@ProxyEvent
	public void onLoad(LoadStudyEvent event) {
		study = event.getStudy();
		if (!study.getId().equals(studyId))
			gwasPlotsLoaded = false;
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id", study.getId().toString());
		String historyToken  = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		getProxy().changeTab(new TabDataDynamic("Plot ("+study.getProtocol().getAnalysisMethod()+")", tabData.getPriority(), historyToken));
	}
}
