package com.gmi.nordborglab.browser.client.mvp.diversity.study.snp;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class SNPDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(SNPDetailPresenter.class, SNPDetailPresenter.MyView.class, SNPDetailView.class, SNPDetailPresenter.MyProxy.class);
    }
}
