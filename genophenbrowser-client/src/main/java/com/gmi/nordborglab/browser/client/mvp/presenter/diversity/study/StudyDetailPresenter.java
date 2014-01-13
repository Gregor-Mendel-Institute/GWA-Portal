package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.gmi.nordborglab.browser.client.events.*;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.Statistics;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.util.PhenotypeHistogram;
import com.google.common.collect.*;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyDetailView.StudyDisplayDriver;
import com.gmi.nordborglab.browser.client.mvp.view.diversity.study.StudyDetailView.StudyEditDriver;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
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
        Presenter<StudyDetailPresenter.MyView, StudyDetailPresenter.MyProxy> implements StudyDetailUiHandlers {

    public interface MyView extends View, HasUiHandlers<StudyDetailUiHandlers> {

        StudyDisplayDriver getDisplayDriver();

        void scheduledLayout();

        void setGeoChartData(Multiset<String> geochartData);

        void setPhenotypExplorerData(ImmutableSet<TraitProxy> traits);

        void setHistogramChartData(ImmutableSortedMap<Double, Integer> data);

        StudyEditDriver getEditDriver();

        void showGWASUploadPopup(boolean show);

        void showJobInfo(StudyJobProxy job, int permissionMask);

        void showActionBtns(boolean show);

        void showEditPopup(boolean show);

        void showDeletePopup(boolean show);
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
        super(eventBus, view, proxy, StudyTabPresenter.TYPE_SetTabContent);
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        this.gwasUploadWizardPresenterWidget = gwasUploadWizardPresenterWidget;
        this.cdvManager = cdvManager;
        this.currentUser = currentUser;
        receiver = new Receiver<StudyProxy>() {
            public void onSuccess(StudyProxy response) {
                fireEvent(new LoadingIndicatorEvent(false));
                study = response;
                fireEvent(new LoadStudyEvent(study));
                getView().showEditPopup(false);
                getView().getDisplayDriver().display(study);
            }


            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                onEdit();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onConstraintViolation(violations);
            }
        };
    }


    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SetGWASUploadContent, gwasUploadWizardPresenterWidget);
        registerHandler(GWASUploadedEvent.register(getEventBus(), new GWASUploadedEvent.Handler() {
            @Override
            public void onGWASUploaded(GWASUploadedEvent event) {
                getView().showGWASUploadPopup(false);
                cdvManager.findOne(new Receiver<StudyProxy>() {
                    @Override
                    public void onSuccess(StudyProxy response) {
                        study = response;
                        getView().showJobInfo(study.getJob(), currentUser.getPermissionMask(study.getUserPermission()));
                    }
                }, study.getId());
            }
        }));
        registerHandler(getEventBus().addHandler(LoadStudyEvent.TYPE, new LoadStudyEvent.LoadStudyHandler() {
            @Override
            public void onLoad(LoadStudyEvent event) {
                if (study == null || study.getId().equals(event.getStudy().getId())) {
                    study = event.getStudy();
                    getView().showJobInfo(study.getJob(), currentUser.getPermissionMask(study.getUserPermission()));
                }
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();
        gwasUploadWizardPresenterWidget.setMultipleUpload(false);
        gwasUploadWizardPresenterWidget.setRestURL("/provider/study/" + study.getId() + "/upload");
        if (fireLoadEvent) {
            fireLoadEvent = false;
            fireEvent(new LoadStudyEvent(study));
        }
        getView().getDisplayDriver().display(study);
        getView().showActionBtns(currentUser.hasEdit(study));
        getProxy().getTab().setTargetHistoryToken(
                placeManager.buildHistoryToken(placeManager
                        .getCurrentPlaceRequest()));
        calculateGeoChartData();
        calculateHistogramData();
        getView().showJobInfo(study.getJob(), currentUser.getPermissionMask(study.getUserPermission()));
        getView().setGeoChartData(geochartData);
        getView().setHistogramChartData(histogramData);
        getView().scheduledLayout();
        getView().setPhenotypExplorerData(ImmutableSet.copyOf(study.getTraits()));
        fireEvent(new LoadingIndicatorEvent(false));
    }


    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        fireEvent(new LoadingIndicatorEvent(true));
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
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
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
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @ProxyEvent
    public void onLoad(LoadStudyEvent event) {
        study = event.getStudy();
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", study.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(
                new TabDataDynamic(tabData.getLabel(), tabData.getPriority(),
                        historyToken));
    }

    private void calculateHistogramData() {
        List<Double> traitValues = Lists.transform(Lists.newArrayList(study.getTraits()), Statistics.traitToDouble);
        histogramData = PhenotypeHistogram.getHistogram(traitValues, BIN_COUNT);
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
        CdvRequest ctx = cdvManager.getContext();
        getView().getEditDriver().edit(study, ctx);
        ctx.saveStudy(study).with(CdvManager.FULL_PATH).to(receiver);
        getView().showEditPopup(true);
    }

    @Override
    public void onSave() {
        RequestContext req = getView().getEditDriver().flush();
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
        req.fire();

    }

    @Override
    public void onCancel() {
        getView().showEditPopup(false);
    }

    @Override
    public void onDelete() {
        getView().showDeletePopup(true);

    }

    @Override
    public void onStartAnalysis() {
        if (study.getJob() != null)
            return;
        cdvManager.createStudyJob(new Receiver<StudyProxy>() {

            @Override
            public void onSuccess(StudyProxy response) {
                study = response;
                getView().showJobInfo(study.getJob(), currentUser.getPermissionMask(study.getUserPermission()));
                StudyModifiedEvent.fire(getEventBus(), response);
            }
        }, study.getId());
    }

    @Override
    public void onClickUpload() {
        //gwasUploadWizardPresenterWidget.setMultipleUpload(false);
        //gwasUploadWizardPresenterWidget.setRestURL("/provider/study/" + study.getId() + "/upload");
    }

    @Override
    public void onConfirmDelete() {
        fireEvent(new LoadingIndicatorEvent(true, "Removing..."));
        cdvManager.delete(new Receiver<Void>() {
            @Override
            public void onSuccess(Void response) {
                PlaceRequest request = null;
                if (placeManager.getHierarchyDepth() <= 1) {
                    request = new PlaceRequest.Builder().nameToken(NameTokens.phenotypeoverview).build();
                } else {
                    request = placeManager.getCurrentPlaceHierarchy().get(placeManager.getHierarchyDepth() - 2);
                }
                getView().showDeletePopup(false);
                study = null;
                placeManager.revealPlace(request);
            }

            @Override
            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                super.onFailure(error);    //To change body of overridden methods use File | Settings | File Templates.
            }
        }, study);
    }

    @Override
    public void onDeleteJob() {
        if (study.getJob() == null)
            return;
        cdvManager.deleteStudyJob(new Receiver<StudyProxy>() {

            @Override
            public void onSuccess(StudyProxy response) {
                study = response;
                getView().showJobInfo(study.getJob(), currentUser.getPermissionMask(study.getUserPermission()));
                StudyModifiedEvent.fire(getEventBus(), response);
            }
        }, study.getId());
    }

    @Override
    public void onReRunAnalysis() {
        if (study.getJob() == null || !study.getJob().getStatus().equalsIgnoreCase("Error")) {
            return;
        }
        cdvManager.rerunAnalysis(new Receiver<StudyProxy>() {

            @Override
            public void onSuccess(StudyProxy response) {
                study = response;
                getView().showJobInfo(study.getJob(), currentUser.getPermissionMask(study.getUserPermission()));
                StudyModifiedEvent.fire(getEventBus(), response);
            }
        }, study.getId());
    }


    private int getPermission() {
        int permission = 0;
        if (study != null)
            permission = currentUser.getPermissionMask(study.getUserPermission());
        return permission;
    }

}
