package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.studies;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class StudyListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bindPresenter(StudyListPresenter.class,
                StudyListPresenter.MyView.class, StudyListView.class,
                StudyListPresenter.MyProxy.class);
    }
}
