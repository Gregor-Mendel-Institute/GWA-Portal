package com.gmi.nordborglab.browser.client.mvp;

import com.gmi.nordborglab.browser.client.mvp.account.AccountModule;
import com.gmi.nordborglab.browser.client.mvp.diversity.DiversityModule;
import com.gmi.nordborglab.browser.client.mvp.genotype.GenotypeModule;
import com.gmi.nordborglab.browser.client.mvp.germplasm.GermplasmModule;
import com.gmi.nordborglab.browser.client.mvp.home.HomeTabModule;
import com.gmi.nordborglab.browser.client.mvp.profile.ProfileModule;
import com.gmi.nordborglab.browser.client.mvp.users.UserListModule;
import com.gmi.nordborglab.browser.client.mvp.widgets.WidgetsModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/23/15.
 */
public class ApplicationModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        install(new DiversityModule());
        install(new GermplasmModule());
        install(new AccountModule());
        install(new HomeTabModule());
        install(new GenotypeModule());
        install(new ProfileModule());
        install(new UserListModule());
        install(new WidgetsModule());

        bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class,
                ApplicationView.class, ApplicationPresenter.MyProxy.class);
    }
}
