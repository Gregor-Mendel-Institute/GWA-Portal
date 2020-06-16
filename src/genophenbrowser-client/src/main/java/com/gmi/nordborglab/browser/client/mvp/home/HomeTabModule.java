package com.gmi.nordborglab.browser.client.mvp.home;

import com.gmi.nordborglab.browser.client.mvp.home.dashboard.DashboardModule;
import com.gmi.nordborglab.browser.client.mvp.home.landingpage.HomeModule;
import com.gmi.nordborglab.browser.client.mvp.home.wizard.BasicStudyWizardModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class HomeTabModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new DashboardModule());
        install(new HomeModule());
        install(new BasicStudyWizardModule());

        bindPresenter(HomeTabPresenter.class, HomeTabPresenter.MyView.class, HomeTabView.class, HomeTabPresenter.MyProxy.class);
    }
}
