package com.gmi.nordborglab.browser.client.mvp.germplasm.passport.detail;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class PassportDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(PassportDetailPresenter.class,
                PassportDetailPresenter.MyView.class, PassportDetailView.class,
                PassportDetailPresenter.MyProxy.class);
    }
}
