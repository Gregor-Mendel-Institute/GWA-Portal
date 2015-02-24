package com.gmi.nordborglab.browser.client.mvp.diversity;


import com.gmi.nordborglab.browser.client.mvp.diversity.experiment.ExperimentDetailTabModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.experiments.ExperimentOverviewModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.meta.MetaModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.ontology.TraitOntologyModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.PhenotypeDetailTabModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.phenotypes.PhenotypeOverviewModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.publication.detail.PublicationDetailModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.publication.list.PublicationOverviewModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.studies.StudyOverviewModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.StudyTabModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.tools.ToolsModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class DiversityModule extends AbstractPresenterModule {

    @Override
    protected void configure() {

        install(new ExperimentDetailTabModule());
        install(new ExperimentOverviewModule());
        install(new PhenotypeDetailTabModule());
        install(new PhenotypeOverviewModule());
        install(new StudyTabModule());
        install(new StudyOverviewModule());
        install(new TraitOntologyModule());
        install(new PublicationDetailModule());
        install(new PublicationOverviewModule());
        install(new MetaModule());
        install(new ToolsModule());


        bindPresenter(DiversityPresenter.class,
                DiversityPresenter.MyView.class, DiversityView.class,
                DiversityPresenter.MyProxy.class);
    }
}
