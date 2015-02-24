package com.gmi.nordborglab.browser.client.mvp.genotype.genomebrowser;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class GenomeBrowserModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(GenomeBrowserPresenter.class,
                GenomeBrowserPresenter.MyView.class, GenomeBrowserView.class,
                GenomeBrowserPresenter.MyProxy.class);
    }
}
