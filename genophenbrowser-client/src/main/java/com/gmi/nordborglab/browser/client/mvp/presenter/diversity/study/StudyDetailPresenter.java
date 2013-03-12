package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import java.util.Set;

import javax.validation.ConstraintViolation;

import com.gmi.nordborglab.browser.client.events.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyDetailView.StudyDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyDetailView.StudyEditDriver;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class StudyDetailPresenter extends
		Presenter<StudyDetailPresenter.MyView, StudyDetailPresenter.MyProxy> implements StudyDetailUiHandlers{

	public interface MyView extends View,HasUiHandlers<StudyDetailUiHandlers> {

		StudyDisplayDriver getDisplayDriver();

		void setState(State displaying, int permissionMask);

		void scheduledLayout();

		void setGeoChartData(Multiset<String> geochartData);

		void setPhenotypExplorerData(ImmutableSet<TraitProxy> traits);

		void setHistogramChartData(ImmutableSortedMap<Double, Integer> data);

		StudyEditDriver getEditDriver();

		State getState();

        void showGWASUploadPopup(boolean show);

        void showGWASBtns(boolean show);

        void showJobInfo(StudyJobProxy job, int permissionMask);
    }

	protected StudyProxy study;
	protected boolean fireLoadEvent;
	protected final PlaceManager placeManager;
	protected final CdvManager cdvManager;
	protected final CurrentUser currentUser;
	private ImmutableSortedMap<Double, Integer> histogramData;
	private Multiset<String> geochartData;
	private static int BIN_COUNT = 20;
	protected final Receiver<StudyProxy> receiver;
    protected final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget;

    public static final Object TYPE_SetGWASUploadContent = new Object();

	public enum LOWER_CHART_TYPE {
		histogram, explorer
	}

	public enum UPPER_CHART_TYPE {
		geochart, piechart
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.study)
	@TabInfo(label = "Overview", priority = 0, container = StudyTabPresenter.class)
	public interface MyProxy extends TabContentProxyPlace<StudyDetailPresenter> {
	}

	@Inject
	public StudyDetailPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final PlaceManager placeManager,
			final CdvManager cdvManager, final CurrentUser currentUser,
            final GWASUploadWizardPresenterWidget gwasUploadWizardPresenterWidget) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		this.placeManager = placeManager;
        this.gwasUploadWizardPresenterWidget = gwasUploadWizardPresenterWidget;
		this.cdvManager = cdvManager;
		this.currentUser = currentUser;
		receiver = new Receiver<StudyProxy>() {
			public void onSuccess(StudyProxy response) {
				study = response;
				fireEvent(new LoadStudyEvent(study));
				getView().setState(State.DISPLAYING,getPermission());
				getView().getDisplayDriver().display(study);
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
		RevealContentEvent.fire(this, StudyTabPresenter.TYPE_SetTabContent,
				this);
	}

	@Override
	protected void onBind() {
		super.onBind();
        setInSlot(TYPE_SetGWASUploadContent,gwasUploadWizardPresenterWidget);
        registerHandler(GWASUploadedEvent.register(getEventBus(), new GWASUploadedEvent.Handler() {
            @Override
            public void onGWASUploaded(GWASUploadedEvent event) {
                getView().showGWASUploadPopup(false);
                getView().showGWASBtns(false);
                cdvManager.findOne(new Receiver<StudyProxy>() {
                    @Override
                    public void onSuccess(StudyProxy response) {
                        study = response;
                    }
                },study.getId());
            }
        }));
	}

	@Override
	protected void onReset() {
		super.onReset();
		if (fireLoadEvent) {
			fireLoadEvent = false;
			fireEvent(new LoadStudyEvent(study));
		}
		getView().getDisplayDriver().display(study);
		getView().setState(State.DISPLAYING,
				currentUser.getPermissionMask(study.getUserPermission()));
		getProxy().getTab().setTargetHistoryToken(
				placeManager.buildHistoryToken(placeManager
						.getCurrentPlaceRequest()));
		LoadingIndicatorEvent.fire(this, false);
		calculateGeoChartData();
		calculateHistogramData();
        getView().showJobInfo(study.getJob(),currentUser.getPermissionMask(study.getUserPermission()));
		getView().setGeoChartData(geochartData);
		getView().setHistogramChartData(histogramData);
		getView().scheduledLayout();
		getView().setPhenotypExplorerData(ImmutableSet.copyOf(study.getTraits()));
        gwasUploadWizardPresenterWidget.setMultipleUpload(false);
        gwasUploadWizardPresenterWidget.setRestURL("/provider/study/" + study.getId() + "/upload");
	}

	@Override
	public boolean useManualReveal() {
		return true;
	}

	@Override
	public void prepareFromRequest(PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		LoadingIndicatorEvent.fire(this, true);
		Receiver<StudyProxy> receiver = new Receiver<StudyProxy>() {
			@Override
			public void onSuccess(StudyProxy response) {
				study = response;
				fireLoadEvent = true;
				getProxy().manualReveal(StudyDetailPresenter.this);
			}

			@Override
			public void onFailure(ServerFailure error) {
				getProxy().manualRevealFailed();
				placeManager.revealPlace(new PlaceRequest(
						NameTokens.experiments));
			}
		};
		try {
			Long studyId = Long.valueOf(placeRequest.getParameter("id", null));
			if (study == null || !study.getId().equals(studyId)) {
				cdvManager.findOne(receiver, studyId);
			} else {
				getProxy().manualReveal(StudyDetailPresenter.this);
			}
		} catch (NumberFormatException e) {
			getProxy().manualRevealFailed();
			placeManager.revealPlace(new PlaceRequest(NameTokens.experiments));
		}
	}

	@ProxyEvent
	public void onLoad(LoadStudyEvent event) {
		study = event.getStudy();
		PlaceRequest request = new ParameterizedPlaceRequest(getProxy()
				.getNameToken()).with("id", study.getId().toString());
		String historyToken = placeManager.buildHistoryToken(request);
		TabData tabData = getProxy().getTabData();
		getProxy().changeTab(
				new TabDataDynamic(tabData.getLabel(), tabData.getPriority(),
						historyToken));
	}

	private void calculateHistogramData() {
		SortedMultiset<Double> data = TreeMultiset.create();
		for (TraitProxy trait : study.getTraits()) {
			if (trait.getValue() != null) {
				try {
					data.add(Double.parseDouble(trait.getValue()));
				} catch (NumberFormatException e) {
				}
			}
		}
		if (data.size() == 0)
			return;
		Double min = data.elementSet().first();
		Double max = data.elementSet().last();
		if (min == max)
			return;
		Double binWidth = (max - min) / BIN_COUNT;
		ImmutableSortedMap.Builder<Double, Integer> builder = ImmutableSortedMap
				.naturalOrder();
		for (int i = 0; i < BIN_COUNT; i++) {
			Double lowBound = min + i * binWidth;
			Double upperBound = lowBound + binWidth;
			builder.put(
					lowBound,
					data.subMultiset(lowBound, BoundType.CLOSED,
							upperBound, BoundType.CLOSED).size());
		}
		builder.put(max, 0);
		histogramData = builder.build();
	}

	private void calculateGeoChartData() {
		ImmutableMultiset.Builder<String> builder = ImmutableMultiset.builder();
		for (TraitProxy trait : study.getTraits()) {
			try {
				String cty = trait.getObsUnit().getStock().getPassport()
						.getCollection().getLocality().getCountry();
				builder.add(cty);
			} catch (NullPointerException e) {

			}
		}
		geochartData = builder.build();
	}

	

	@Override
	public void onEdit() {
		getView().setState(State.EDITING,getPermission());
		CdvRequest ctx = cdvManager.getContext();
				
		getView().getEditDriver().edit(study, ctx);
		ctx.saveStudy(study).with(CdvManager.FULL_PATH).to(receiver);
		
	}

	@Override
	public void onSave() {
		getView().setState(State.SAVING,getPermission());
		RequestContext req = getView().getEditDriver().flush();
		req.fire();
		
	}

	@Override
	public void onCancel() {
		getView().setState(State.DISPLAYING,getPermission());
		getView().getDisplayDriver().display(study);
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onStartAnalysis() {
        if (study.getJob() != null)
            return;
        cdvManager.createStudyJob(new Receiver<StudyProxy>() {

            @Override
            public void onSuccess(StudyProxy response) {
                study = response;
                getView().showGWASBtns(false);
                StudyModifiedEvent.fire(getEventBus(), response);
            }
        }, study.getId());
    }

    private int getPermission() {
		int permission = 0;
		if (study != null) 
		    permission =  currentUser.getPermissionMask(study.getUserPermission());
		return permission;
	}

}
