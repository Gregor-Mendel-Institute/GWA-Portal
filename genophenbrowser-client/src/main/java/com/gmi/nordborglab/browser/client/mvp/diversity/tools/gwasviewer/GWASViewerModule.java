package com.gmi.nordborglab.browser.client.mvp.diversity.tools.gwasviewer;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class GWASViewerModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(GWASViewerPresenter.class, GWASViewerPresenter.MyView.class, GWASViewerView.class, GWASViewerPresenter.MyProxy.class);
    }
}
