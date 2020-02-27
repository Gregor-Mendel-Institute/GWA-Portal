package com.gmi.nordborglab.browser.client.mvp.germplasm;

import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.detail.PassportDetailModule;
import com.gmi.nordborglab.browser.client.mvp.germplasm.passport.list.PassportListModule;
import com.gmi.nordborglab.browser.client.mvp.germplasm.stock.StockDetailModule;
import com.gmi.nordborglab.browser.client.mvp.germplasm.taxonomy.detail.TaxonomyDetailModule;
import com.gmi.nordborglab.browser.client.mvp.germplasm.taxonomy.list.TaxonomyOverviewModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class GermplasmModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        install(new PassportDetailModule());
        install(new PassportListModule());
        install(new StockDetailModule());
        install(new TaxonomyOverviewModule());
        install(new TaxonomyDetailModule());

        bindPresenter(GermplasmPresenter.class,
                GermplasmPresenter.MyView.class, GermplasmView.class,
                GermplasmPresenter.MyProxy.class);
    }
}
