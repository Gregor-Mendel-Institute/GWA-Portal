package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 3/3/15.
 */
public class SNPViewerModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(SNPViewerPresenter.class, SNPViewerPresenter.MyView.class, SNPViewerView.class,
                SNPViewerPresenter.MyProxy.class);
    }
}
