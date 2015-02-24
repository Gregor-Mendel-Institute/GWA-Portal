package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype;

import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail.PhenotypeDetailModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.enrichments.PhenotypeCandidateGeneListEnrichmentModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.studies.StudyListModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload.PhenotypeUploadWizardPresenterWidget;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.upload.PhenotypeUploadWizardView;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.wizard.StudyWizardModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class PhenotypeDetailTabModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new PhenotypeDetailModule());
        install(new PhenotypeCandidateGeneListEnrichmentModule());
        install(new StudyListModule());
        install(new StudyWizardModule());

        bindPresenterWidget(PhenotypeUploadWizardPresenterWidget.class, PhenotypeUploadWizardPresenterWidget.MyView.class, PhenotypeUploadWizardView.class);

        bindPresenter(PhenotypeDetailTabPresenter.class,
                PhenotypeDetailTabPresenter.MyView.class,
                PhenotypeDetailTabView.class,
                PhenotypeDetailTabPresenter.MyProxy.class);

    }
}
