package com.gmi.nordborglab.browser.client.mvp.germplasm.stock;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class StockDetailModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(StockDetailPresenter.class,
                StockDetailPresenter.MyView.class, StockDetailView.class,
                StockDetailPresenter.MyProxy.class);
    }
}
