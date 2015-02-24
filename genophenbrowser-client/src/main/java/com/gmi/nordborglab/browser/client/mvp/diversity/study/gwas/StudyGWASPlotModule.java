package com.gmi.nordborglab.browser.client.mvp.diversity.study.gwas;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StudyGWASPlotModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(StudyGWASPlotPresenter.class,
                StudyGWASPlotPresenter.MyView.class, StudyGWASPlotView.class,
                StudyGWASPlotPresenter.MyProxy.class);
    }
}
