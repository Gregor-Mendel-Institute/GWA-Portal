package com.gmi.nordborglab.browser.client.mvp.users;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class UserListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bindPresenter(UserListPresenter.class,
                UserListPresenter.MyView.class, UserListView.class,
                UserListPresenter.MyProxy.class);
    }
}
