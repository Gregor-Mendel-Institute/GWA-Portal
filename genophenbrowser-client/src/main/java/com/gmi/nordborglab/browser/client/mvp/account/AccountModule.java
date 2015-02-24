package com.gmi.nordborglab.browser.client.mvp.account;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class AccountModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(AccountPresenter.class,
                AccountPresenter.MyView.class, AccountView.class, AccountPresenter.MyProxy.class);

    }
}
