package com.gmi.nordborglab.browser.client.mvp.diversity.ontology;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class TraitOntologyModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(TraitOntologyPresenter.class,
                TraitOntologyPresenter.MyView.class,
                TraitOntologyView.class,
                TraitOntologyPresenter.MyProxy.class);
    }
}
