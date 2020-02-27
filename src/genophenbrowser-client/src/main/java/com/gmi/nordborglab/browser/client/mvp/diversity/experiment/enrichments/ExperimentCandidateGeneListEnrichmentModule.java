package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.enrichments;

import com.gmi.nordborglab.browser.client.mvp.widgets.enrichment.CandidateGeneListEnrichmentView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class ExperimentCandidateGeneListEnrichmentModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(ExperimentCandidateGeneListEnrichmentPresenter.class,
                ExperimentCandidateGeneListEnrichmentPresenter.MyView.class,
                CandidateGeneListEnrichmentView.class,
                ExperimentCandidateGeneListEnrichmentPresenter.MyProxy.class);
    }
}
