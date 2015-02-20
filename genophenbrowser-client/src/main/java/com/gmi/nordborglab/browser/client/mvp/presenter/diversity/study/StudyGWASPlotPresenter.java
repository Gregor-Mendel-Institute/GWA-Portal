package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataAction;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.SelectSNPEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASPlotPresenterWidget;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class StudyGWASPlotPresenter
        extends
        Presenter<StudyGWASPlotPresenter.MyView, StudyGWASPlotPresenter.MyProxy> {

    public interface MyView extends View {

        void showSNPPopUp(Long analysisId, SelectSNPEvent event);

        void setAnalysisId(Long id);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.studygwas)
    @TabInfo(label = "GWAS-Plots", priority = 1, container = StudyTabPresenter.class)
    public interface MyProxy extends TabContentProxyPlace<StudyGWASPlotPresenter> {
    }

    @ContentSlot
    public static final GwtEvent.Type<RevealContentHandler<?>> TYPE_SetGWASPlotsContent = new GwtEvent.Type<RevealContentHandler<?>>();

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
        super(eventBus, view, proxy, StudyTabPresenter.TYPE_SetTabContent);
        this.gwasPlotPresenterWidget = gwasPlotPresenterWidget;
        this.placeManager = placeManager;
        this.cdvManager = cdvManager;
    }


    @Override
    protected void onBind() {
        super.onBind();
        setInSlot(TYPE_SetGWASPlotsContent, gwasPlotPresenterWidget);
        registerHandler(getEventBus().addHandlerToSource(SelectSNPEvent.TYPE, gwasPlotPresenterWidget, new SelectSNPEvent.Handler() {
            @Override
            public void onSelectSNP(SelectSNPEvent event) {
                getView().showSNPPopUp(studyId, event);
            }
        }));
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (fireLoadEvent) {
            fireEvent(new LoadStudyEvent(study));
            fireLoadEvent = false;
        }
        gwasPlotPresenterWidget.loadPlots(studyId, GetGWASDataAction.TYPE.STUDY);
        getView().setAnalysisId(studyId);
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        LoadingIndicatorEvent.fire(this, true);
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
                        fireLoadEvent = true;
                    }
                }, studyIdToLoad);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @ProxyEvent
    public void onLoad(LoadStudyEvent event) {
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
}
