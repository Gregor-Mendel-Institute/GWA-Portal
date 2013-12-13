package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProvider;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProviderImpl;
import com.gmi.nordborglab.browser.client.mvp.presenter.CandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListEnrichmentPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;
import java.util.Set;

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
        PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id", study.getId().toString());
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
            placeManager.revealPlace(new ParameterizedPlaceRequest(NameTokens.experiments));
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

}
