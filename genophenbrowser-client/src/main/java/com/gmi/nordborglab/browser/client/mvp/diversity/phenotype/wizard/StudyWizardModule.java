package com.gmi.nordborglab.browser.client.mvp.diversity.phenotype.wizard;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StudyWizardModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(StudyWizardPresenter.class,
                StudyWizardPresenter.MyView.class, StudyWizardView.class,
                StudyWizardPresenter.MyProxy.class);
    }
}
