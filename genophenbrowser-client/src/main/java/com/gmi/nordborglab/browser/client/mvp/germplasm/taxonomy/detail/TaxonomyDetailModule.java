package com.gmi.nordborglab.browser.client.mvp.germplasm.taxonomy.detail;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class TaxonomyDetailModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(TaxonomyDetailPresenter.class,
                TaxonomyDetailPresenter.MyView.class, TaxonomyDetailView.class,
                TaxonomyDetailPresenter.MyProxy.class);
    }
}
