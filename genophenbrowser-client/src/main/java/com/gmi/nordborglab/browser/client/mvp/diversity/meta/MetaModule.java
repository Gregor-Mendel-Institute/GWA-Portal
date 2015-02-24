package com.gmi.nordborglab.browser.client.mvp.diversity.meta;

import com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail.CandidateGeneListDetailModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.list.CandidateGeneListModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes.MetaAnalysisGeneModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.meta.topsnps.MetaAnalysisTopResultsModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class MetaModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        install(new MetaAnalysisGeneModule());
        install(new MetaAnalysisTopResultsModule());
        install(new CandidateGeneListDetailModule());
        install(new CandidateGeneListModule());
    }
}
