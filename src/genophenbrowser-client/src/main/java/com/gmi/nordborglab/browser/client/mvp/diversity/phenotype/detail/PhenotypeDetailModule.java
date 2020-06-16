package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.detail;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class PhenotypeDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bindPresenter(PhenotypeDetailPresenter.class,
                PhenotypeDetailPresenter.MyView.class,
                PhenotypeDetailView.class,
                PhenotypeDetailPresenter.MyProxy.class);
    }
}
