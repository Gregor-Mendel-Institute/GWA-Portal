package com.gmi.nordborglab.browser.client.mvp.diversity.study.gwas;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.events.GWASDataLoadedEvent;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.StudyTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.gwas.GWASPlotPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class StudyGWASPlotPresenter
        extends
        Presenter<StudyGWASPlotPresenter.MyView, StudyGWASPlotPresenter.MyProxy> implements StudyGWASPlotUiHandlers {

    public interface MyView extends View, HasUiHandlers<StudyGWASPlotUiHandlers> {

        void showSNPPopUp(Long analysisId, SelectSNPEvent event);

        void setHasLdData(boolean hasLdData);
    }
    @ProxyCodeSplit
    @NameToken(NameTokens.studygwas)
    @TabInfo(label = "GWAS-Plots", priority = 1, container = StudyTabPresenter.class)
    public interface MyProxy extends TabContentProxyPlace<StudyGWASPlotPresenter> {

    }
    static final SingleSlot<GWASPlotPresenterWidget> SLOT_GWAS_PLOT = new SingleSlot<>();

    protected StudyProxy study;

    protected Long studyId;
    protected boolean gwasPlotsLoaded = false;
    protected final PlaceManager placeManager;
    protected boolean fireLoadEvent = false;
    protected final CdvManager cdvManager;
    private final GWASPlotPresenterWidget gwasPlotPresenterWidget;
    @Inject
    public StudyGWASPlotPresenter(final EventBus eventBus, final MyView view,
                                  final MyProxy proxy, final PlaceManager placeManager,
                                  final CdvManager cdvManager,
                                  final GWASPlotPresenterWidget gwasPlotPresenterWidget) {
        super(eventBus, view, proxy, StudyTabPresenter.SLOT_CONTENT);
        getView().setUiHandlers(this);
        this.gwasPlotPresenterWidget = gwasPlotPresenterWidget;
        this.placeManager = placeManager;
        this.cdvManager = cdvManager;
    }

    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(SLOT_GWAS_PLOT, gwasPlotPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(SelectSNPEvent.TYPE, gwasPlotPresenterWidget, event -> getView().showSNPPopUp(studyId, event)));
        registerHandler(getEventBus().addHandlerToSource(GWASDataLoadedEvent.TYPE, gwasPlotPresenterWidget, event -> {
            getView().setHasLdData(event.getGwasData().hasLdData());
        }));
    }


    @Override
    protected void onReset() {
        super.onReset();
        gwasPlotPresenterWidget.loadPlots(studyId, GetGWASDataAction.TYPE.STUDY);
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        try {
            final Long studyIdToLoad = Long.valueOf(placeRequest.getParameter("id", null));
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
                        fireEvent(new LoadStudyEvent(study));
                    }
                }, studyIdToLoad);
            }
        } catch (NumberFormatException e) {

            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @ProxyEvent
    public void onLoadStudy(LoadStudyEvent event) {
        study = event.getStudy();
        if (!study.getId().equals(studyId))
            gwasPlotsLoaded = false;
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", study.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        TabDataDynamic newTabData = new TabDataDynamic("Plot (" + study.getProtocol().getAnalysisMethod() + ")", tabData.getPriority(), historyToken);
        boolean hasPlots = study.getJob() != null && study.getJob().getStatus().equalsIgnoreCase("Finished");
        newTabData.setHasAccess(hasPlots);
        getProxy().changeTab(newTabData);
    }

    @Override
    public void showLdForSNP(String chromosome, Integer position) {
        gwasPlotPresenterWidget.loadLdForSnp(chromosome, position);
    }

    @Override
    public void showExactLdForRegion(String chromosome, Integer position) {
        gwasPlotPresenterWidget.loadExactLdForRegion(chromosome, position);
    }

    @Override
    public void showLdForRegion(String chromosome, Integer position) {
        gwasPlotPresenterWidget.loadLdForRegion(chromosome, position, 500);
    }
}
