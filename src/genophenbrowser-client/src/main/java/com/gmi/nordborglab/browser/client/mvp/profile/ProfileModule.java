package com.gmi.nordborglab.browser.client.mvp.profile;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class ProfileModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(ProfilePresenter.class, ProfilePresenter.MyView.class, ProfileView.class, ProfilePresenter.MyProxy.class);
    }
}
