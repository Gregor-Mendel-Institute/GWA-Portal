package com.gmi.nordborglab.browser.client.mvp.diversity.experiment;

import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail.ExperimentDetailModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.enrichments.ExperimentCandidateGeneListEnrichmentModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes.PhenotypeListModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class ExperimentDetailTabModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        install(new ExperimentDetailModule());
        install(new PhenotypeListModule());
        install(new ExperimentCandidateGeneListEnrichmentModule());

        bindPresenter(ExperimentDetailTabPresenter.class,
                ExperimentDetailTabPresenter.MyView.class,
                ExperimentDetailTabView.class,
                ExperimentDetailTabPresenter.MyProxy.class);
    }
}
