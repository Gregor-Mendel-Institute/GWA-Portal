package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.phenotypes;


import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class PhenotypeListModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(PhenotypeListPresenter.class,
                PhenotypeListPresenter.MyView.class, PhenotypeListView.class,
                PhenotypeListPresenter.MyProxy.class);
    }
}
