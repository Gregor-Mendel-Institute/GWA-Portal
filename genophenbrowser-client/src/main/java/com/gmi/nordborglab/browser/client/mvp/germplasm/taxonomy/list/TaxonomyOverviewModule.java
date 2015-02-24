package com.gmi.nordborglab.browser.client.mvp.germplasm.taxonomy.list;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class TaxonomyOverviewModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(TaxonomyOverviewPresenter.class,
                TaxonomyOverviewPresenter.MyView.class,
                TaxonomyOverviewView.class,
                TaxonomyOverviewPresenter.MyProxy.class);
    }
}
