package com.gmi.nordborglab.browser.client.mvp.diversity.publication.list;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class PublicationOverviewModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(PublicationOverviewPresenter.class,
                PublicationOverviewPresenter.MyView.class, PublicationOverviewView.class,
                PublicationOverviewPresenter.MyProxy.class);
    }
}
