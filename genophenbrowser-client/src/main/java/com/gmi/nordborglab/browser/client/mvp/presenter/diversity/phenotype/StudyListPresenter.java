package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyListUiHandlers;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.Callback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
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

public class StudyListPresenter extends
		Presenter<StudyListPresenter.MyView, StudyListPresenter.MyProxy> implements StudyListUiHandlers{

	public interface MyView extends View,HasUiHandlers<StudyListUiHandlers> {

		HasData<StudyProxy> getDisplay();

		void showAddBtn(boolean showAdd);
	}
	
	protected PhenotypeProxy phenotype;
	protected Long phenotypeId;
	protected boolean studiesLoaded = false; 
	protected final PlaceManager placeManager;
	protected final PhenotypeManager phenotypeManager;
	protected final CdvManager cdvManager;
	protected boolean fireLoadEvent = false;
	protected final AsyncDataProvider<StudyProxy> dataProvider;
	protected final CurrentUser currentUser;

	@ProxyCodeSplit
	@NameToken(NameTokens.studylist)
	@TabInfo(label="Studies",priority=2,container=PhenotypeDetailTabPresenter.class)
	public interface MyProxy extends TabContentProxyPlace<StudyListPresenter> {
	}

	@Inject
	public StudyListPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PlaceManager placeManager,
			final PhenotypeManager phenotypeManager, final CdvManager cdvManager, 
			final CurrentUser currentUser) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		this.currentUser= currentUser;
		this.placeManager = placeManager;
		this.phenotypeManager = phenotypeManager;
		this.cdvManager = cdvManager;
		dataProvider = new AsyncDataProvider<StudyProxy>() {

			@Override
			protected void onRangeChanged(HasData<StudyProxy> display) {
				requestStudies(null,display.getVisibleRange());
			}
		};
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				PhenotypeDetailTabPresenter.TYPE_SetTabContent, this);
	}
	@Override
	protected void onReset() {
		super.onReset();
		if (fireLoadEvent) {
			fireEvent(new LoadPhenotypeEvent(phenotype));
			fireLoadEvent = false;
		}
		LoadingIndicatorEvent.fire(this, false);
		checkPermissionAndUpdateView();
	}

	@Override
	protected void onBind() {
		super.onBind();
		dataProvider.addDataDisplay(getView().getDisplay());
	}
	
	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		try {
			final Long phenotypeIdToLoad = Long.valueOf(placeRequest.getParameter("id",null));
			if (!phenotypeIdToLoad.equals(phenotypeId)) {
				phenotypeId = phenotypeIdToLoad;
				studiesLoaded = false;
			}
			if (studiesLoaded) {
				getProxy().manualReveal(StudyListPresenter.this);
				return;
			}
			if (phenotype == null || !phenotype.getId().equals(phenotypeIdToLoad)) {
				phenotypeManager.getContext().findPhenotype(phenotypeIdToLoad).with("userPermission").fire(new Receiver<PhenotypeProxy>() {
	
					@Override
					public void onSuccess(PhenotypeProxy response) {
						phenotype = response;
						fireLoadEvent = true;
					}
				});
			}
			requestStudies(new Callback<Void, Void>() {

				@Override
				public void onFailure(Void reason) {
					getProxy().manualRevealFailed();
					placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.phenotype).with("id", phenotypeIdToLoad.toString()));
				}

				@Override
				public void onSuccess(Void result) {
					getProxy().manualReveal(StudyListPresenter.this);
				}
			},getView().getDisplay().getVisibleRange());
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.experiments));
		}
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
	
	@ProxyEvent
	void onLoad(LoadPhenotypeEvent event) {
		phenotype = event.getPhenotype();
		if (!phenotype.getId().equals(phenotypeId))
			studiesLoaded = false;
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id", phenotype.getId().toString());
		String historyToken  = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		getProxy().changeTab(new TabDataDynamic("Studies ("+phenotype.getNumberOfStudies()+")", tabData.getPriority(), historyToken));
	}
	
	protected void requestStudies(final Callback<Void, Void> callback,final Range range) {
		if (phenotypeId == null)
			return;
		Receiver<StudyPageProxy> receiver = new Receiver<StudyPageProxy>() {
			@Override
			public void onSuccess(StudyPageProxy obsUnits) {
				dataProvider.updateRowCount(
						(int) obsUnits.getTotalElements(), true);
				dataProvider.updateRowData(range.getStart(), obsUnits.getContent());
				studiesLoaded = true;
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
		cdvManager.findStudiesByPhenotypeId(receiver,phenotypeId, range.getStart(), range.getLength());
	}

	@Override
	public void onNewStudy() {
		PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.studywizard).with("id", phenotypeId.toString());
		placeManager.revealPlace(request);
	}
	
	protected void checkPermissionAndUpdateView() {
		int permission = currentUser.getPermissionMask(phenotype.getUserPermission());
		boolean showAdd = (((permission & AccessControlEntryProxy.CREATE) == AccessControlEntryProxy.CREATE) || 
			((permission & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION));
		getView().showAddBtn(showAdd);
	}
}
