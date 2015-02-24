package com.gmi.nordborglab.browser.client.mvp.diversity.experiment.detail;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class ExperimentDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(ExperimentDetailPresenter.class,
                ExperimentDetailPresenter.MyView.class,
                ExperimentDetailView.class,
                ExperimentDetailPresenter.MyProxy.class);
    }
}
