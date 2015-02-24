package com.gmi.nordborglab.browser.client.mvp.diversity.experiments;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class ExperimentOverviewModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bindPresenter(ExperimentsOverviewPresenter.class,
                ExperimentsOverviewPresenter.MyView.class,
                ExperimentsOverviewView.class,
                ExperimentsOverviewPresenter.MyProxy.class);

        bindPresenter(ExperimentsOverviewTabPresenter.class,
                ExperimentsOverviewTabPresenter.MyView.class,
                ExperimentsOverviewTabView.class,
                ExperimentsOverviewTabPresenter.MyProxy.class);

    }
}
