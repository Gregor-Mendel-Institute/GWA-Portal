package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProvider;
import com.gmi.nordborglab.browser.client.mvp.presenter.CandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 06.12.13
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public class StudyCandidateGeneListEnrichmentPresenter extends CandidateGeneListEnrichmentPresenter<StudyCandidateGeneListEnrichmentPresenter.MyProxy> {


    @ProxyCodeSplit
    @NameToken(NameTokens.studyEnrichments)
    @TabInfo(container = StudyTabPresenter.class,
            label = "Enrichments",
            priority = 2)
    @Title("Enrichments")
    public interface MyProxy extends TabContentProxyPlace<StudyCandidateGeneListEnrichmentPresenter> {

    }


    private StudyProxy study;
    private CdvManager cdvManager;

    @Inject
    public StudyCandidateGeneListEnrichmentPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                                     final CustomRequestFactory rf,
                                                     final PlaceManager placeManager,
                                                     final ClientModule.AssistedInjectionFactory factory,
                                                     final CdvManager cdvManager
    ) {
        super(eventBus, view, proxy, rf, placeManager, factory, factory.createEnrichmentProvider(EnrichmentProvider.TYPE.STUDY), StudyTabPresenter.TYPE_SetTabContent);
        this.cdvManager = cdvManager;
    }

    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
        if (fireLoadEvent) {
            fireEvent(new LoadStudyEvent(study));
            fireLoadEvent = false;
        }
        if (study != dataProvider.getEntity()) {
            dataProvider.setEntity(study);
            candidateGeneListEnrichmentWidget.refresh();
        }
    }


    @ProxyEvent
    public void onLoadStudy(LoadStudyEvent event) {
        study = event.getStudy();
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", study.getId().toString()).build();
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic("Enrichments", tabData.getPriority(), historyToken));
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        try {
            final Long phenotypeToLoadId = Long.valueOf(placeRequest.getParameter("id", null));
            if (study != null && phenotypeToLoadId.equals(study.getId())) {
                getProxy().manualReveal(StudyCandidateGeneListEnrichmentPresenter.this);
                fireLoadEvent = false;
                return;
            }
            LoadingIndicatorEvent.fire(this, true);
            cdvManager.findOne(new Receiver<StudyProxy>() {
                @Override
                public void onSuccess(StudyProxy response) {
                    LoadingIndicatorEvent.fire(StudyCandidateGeneListEnrichmentPresenter.this, false);
                    study = response;
                    fireLoadEvent = true;
                    getProxy().manualReveal(StudyCandidateGeneListEnrichmentPresenter.this);
                    return;
                }
            }, phenotypeToLoadId);
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

}
