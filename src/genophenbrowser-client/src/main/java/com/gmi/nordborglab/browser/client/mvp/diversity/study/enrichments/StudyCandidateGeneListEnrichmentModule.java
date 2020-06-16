package com.gmi.nordborglab.browser.client.mvp.diversity.study.enrichments;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StudyCandidateGeneListEnrichmentModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        //Workaround because CandidateGeneStudyCandidateGeneListEnrichmentPresenterListEnrichmentView was already bound
        bind(StudyCandidateGeneListEnrichmentPresenter.class).in(Singleton.class);
        bind(StudyCandidateGeneListEnrichmentPresenter.MyProxy.class).asEagerSingleton();
    }
}
