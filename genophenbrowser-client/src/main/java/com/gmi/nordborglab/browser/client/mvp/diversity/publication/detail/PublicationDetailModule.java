package com.gmi.nordborglab.browser.client.mvp.diversity.publication.detail;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class PublicationDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(PublicationDetailPresenter.class,
                PublicationDetailPresenter.MyView.class, PublicationDetailView.class,
                PublicationDetailPresenter.MyProxy.class);
    }
}
