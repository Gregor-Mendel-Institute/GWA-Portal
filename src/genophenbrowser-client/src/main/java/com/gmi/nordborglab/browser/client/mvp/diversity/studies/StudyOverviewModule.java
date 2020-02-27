package com.gmi.nordborglab.browser.client.mvp.diversity.studies;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StudyOverviewModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(StudyOverviewPresenter.class,
                StudyOverviewPresenter.MyView.class, StudyOverviewView.class,
                StudyOverviewPresenter.MyProxy.class);
    }
}
