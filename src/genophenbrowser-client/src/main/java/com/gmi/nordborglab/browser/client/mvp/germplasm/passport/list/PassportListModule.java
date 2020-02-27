package com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class PassportListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(PassportListPresenter.class,
                PassportListPresenter.MyView.class, PassportListView.class,
                PassportListPresenter.MyProxy.class);
    }
}
