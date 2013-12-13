package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProvider;
import com.gmi.nordborglab.browser.client.manager.EnrichmentProviderImpl;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.presenter.CandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.CandidateGeneListEnrichmentPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyTabPresenter;
import com.gmi.nordborglab.browser.shared.proxy.*;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 05.12.13
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeCandidateGeneListEnrichmentPresenter extends CandidateGeneListEnrichmentPresenter<PhenotypeCandidateGeneListEnrichmentPresenter.MyProxy> {


    @ProxyCodeSplit
    @NameToken(NameTokens.phenotypeEnrichments)
    @TabInfo(container = PhenotypeDetailTabPresenter.class,
            label = "Enrichments",
            priority = 3)
    @Title("Enrichments")
    public interface MyProxy extends TabContentProxyPlace<PhenotypeCandidateGeneListEnrichmentPresenter> {

    }

    private PhenotypeProxy phenotype;
    private boolean fireLoadEvent = false;
    private final PhenotypeManager phenotypeManager;

    @Inject
    public PhenotypeCandidateGeneListEnrichmentPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                                         final CustomRequestFactory rf,
                                                         final PlaceManager placeManager,
                                                         final ClientModule.AssistedInjectionFactory factory,
                                                         final PhenotypeManager phenotypeManager
    ) {
        super(eventBus, view, proxy, rf, placeManager, factory, factory.createEnrichmentProvider(EnrichmentProvider.TYPE.PHENOTYPE), PhenotypeDetailTabPresenter.TYPE_SetTabContent);
        this.phenotypeManager = phenotypeManager;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, PhenotypeDetailTabPresenter.TYPE_SetTabContent,
                this);
    }


    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
        if (fireLoadEvent) {
            fireEvent(new LoadPhenotypeEvent(phenotype));
            fireLoadEvent = false;
        }
        if (phenotype != dataProvider.getEntity()) {
            dataProvider.setEntity(phenotype);
            candidateGeneListEnrichmentWidget.refresh();
        }
    }


    @ProxyEvent
    public void onLoadPhenotype(LoadPhenotypeEvent event) {
        phenotype = event.getPhenotype();
        PlaceRequest request = new ParameterizedPlaceRequest(getProxy().getNameToken()).with("id", phenotype.getId().toString());
        String historyToken = placeManager.buildHistoryToken(request);
        TabData tabData = getProxy().getTabData();
        getProxy().changeTab(new TabDataDynamic("Enrichments", tabData.getPriority(), historyToken));
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        try {
            final Long phenotypeToLoadId = Long.valueOf(placeRequest.getParameter("id", null));
            if (phenotype != null && phenotypeToLoadId.equals(phenotype.getId())) {
                getProxy().manualReveal(PhenotypeCandidateGeneListEnrichmentPresenter.this);
                fireLoadEvent = false;
                return;
            }
            LoadingIndicatorEvent.fire(this, true);
            phenotypeManager.findOne(new Receiver<PhenotypeProxy>() {
                @Override
                public void onSuccess(PhenotypeProxy response) {
                    LoadingIndicatorEvent.fire(PhenotypeCandidateGeneListEnrichmentPresenter.this, false);
                    phenotype = response;
                    fireLoadEvent = true;
                    getProxy().manualReveal(PhenotypeCandidateGeneListEnrichmentPresenter.this);
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
