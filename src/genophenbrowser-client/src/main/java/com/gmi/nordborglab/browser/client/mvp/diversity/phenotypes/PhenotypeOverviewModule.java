package com.gmi.nordborglab.browser.client.mvp.diversity.phenotypes;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class PhenotypeOverviewModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(PhenotypeOverviewPresenter.class,
                PhenotypeOverviewPresenter.MyView.class,
                PhenotypeOverviewView.class,
                PhenotypeOverviewPresenter.MyProxy.class);
    }
}
