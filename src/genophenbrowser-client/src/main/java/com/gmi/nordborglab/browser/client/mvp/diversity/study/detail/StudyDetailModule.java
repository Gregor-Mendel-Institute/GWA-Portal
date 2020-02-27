package com.gmi.nordborglab.browser.client.mvp.diversity.study.detail;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StudyDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(StudyDetailPresenter.class,
                StudyDetailPresenter.MyView.class, StudyDetailView.class,
                StudyDetailPresenter.MyProxy.class);
    }
}
