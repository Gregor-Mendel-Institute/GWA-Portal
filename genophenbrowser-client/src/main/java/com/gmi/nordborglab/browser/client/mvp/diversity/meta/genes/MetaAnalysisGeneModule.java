package com.gmi.nordborglab.browser.client.mvp.diversity.meta.genes;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class MetaAnalysisGeneModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bindPresenter(MetaAnalysisGenePresenter.class,
                MetaAnalysisGenePresenter.MyView.class, MetaAnalysisGeneView.class,
                MetaAnalysisGenePresenter.MyProxy.class);
    }
}
