package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.enrichments;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.gin.ClientModule;
import com.gmi.nordborglab.browser.client.manager.PhenotypeManager;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.PhenotypeDetailTabPresenter;
import com.gmi.nordborglab.browser.client.mvp.widgets.enrichment.CandidateGeneListEnrichmentPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
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
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

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
        super(eventBus, view, proxy, rf, placeManager, factory, factory.createEnrichmentProvider(ConstEnums.ENRICHMENT_TYPE.PHENOTYPE), PhenotypeDetailTabPresenter.SLOT_CONTENT);
        this.phenotypeManager = phenotypeManager;
    }


    @Override
    protected void onReset() {
        super.onReset();    //To change body of overridden methods use File | Settings | File Templates.
        if (fireLoadEvent) {
            fireEvent(new LoadPhenotypeEvent(phenotype));
            fireLoadEvent = false;
        }
        if (phenotype.getId() != dataProvider.getEntityId()) {
            dataProvider.setEntityId(phenotype.getId());
            candidateGeneListEnrichmentWidget.refresh();
        }
    }


    @ProxyEvent
    public void onLoadPhenotype(LoadPhenotypeEvent event) {
        phenotype = event.getPhenotype();
        PlaceRequest request = new PlaceRequest.Builder()
                .nameToken(getProxy().getNameToken())
                .with("id", phenotype.getId().toString()).build();
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
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.experiments).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }
}
