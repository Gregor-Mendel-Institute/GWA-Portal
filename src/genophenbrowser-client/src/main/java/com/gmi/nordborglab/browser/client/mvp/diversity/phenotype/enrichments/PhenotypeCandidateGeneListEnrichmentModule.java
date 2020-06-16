package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.enrichments;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class PhenotypeCandidateGeneListEnrichmentModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        //Workaround because CandidateGeneListEnrichmentView was already bound
        bind(PhenotypeCandidateGeneListEnrichmentPresenter.class).in(Singleton.class);
        bind(PhenotypeCandidateGeneListEnrichmentPresenter.MyProxy.class).asEagerSingleton();
    }
}
