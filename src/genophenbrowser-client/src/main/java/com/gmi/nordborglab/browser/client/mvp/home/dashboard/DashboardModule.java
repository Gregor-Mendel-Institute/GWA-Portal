package com.gmi.nordborglab.browser.client.mvp.home.dashboard;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class DashboardModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(DashboardPresenter.class,
                DashboardPresenter.MyView.class, DashboardView.class,
                DashboardPresenter.MyProxy.class);
    }
}
