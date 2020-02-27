package com.gmi.nordborglab.browser.client.mvp.diversity.study;

import com.gmi.nordborglab.browser.client.mvp.diversity.study.detail.StudyDetailModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.enrichments.StudyCandidateGeneListEnrichmentModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.gwas.StudyGWASPlotModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.study.snp.SNPDetailModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StudyTabModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new SNPDetailModule());
        install(new StudyGWASPlotModule());
        install(new StudyCandidateGeneListEnrichmentModule());
        install(new StudyDetailModule());

        bindPresenter(StudyTabPresenter.class, StudyTabPresenter.MyView.class,
                StudyTabView.class, StudyTabPresenter.MyProxy.class);
    }
}
