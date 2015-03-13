package com.gmi.nordborglab.browser.client.mvp.genotype;

import com.gmi.nordborglab.browser.client.mvp.genotype.genomebrowser.GenomeBrowserModule;
import com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer.SNPViewerModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class GenotypeModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new GenomeBrowserModule());
        install(new SNPViewerModule());

        bindPresenter(GenotypePresenter.class,
                GenotypePresenter.MyView.class, GenotypeView.class,
                GenotypePresenter.MyProxy.class);
    }
}
