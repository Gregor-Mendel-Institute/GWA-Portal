package com.gmi.nordborglab.browser.client.mvp.home.dashboard;

import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.mvp.home.HomeTabPresenter;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.IsLoggedInGatekeeper;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class DashboardPresenter extends
        Presenter<DashboardPresenter.MyView, DashboardPresenter.MyProxy> {

    public interface MyView extends View {
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.dashboard)
    @UseGatekeeper(IsLoggedInGatekeeper.class)
    public interface MyProxy extends TabContentProxyPlace<DashboardPresenter> {
    }

    @TabInfo(container = HomeTabPresenter.class)
    static TabData getTabLabel(IsLoggedInGatekeeper gateKeeper) {
        return new TabDataDynamic("Dashboard", 1, "",
                gateKeeper);
    }


    @Inject
    public DashboardPresenter(final EventBus eventBus, final MyView view,
                              final MyProxy proxy) {
        super(eventBus, view, proxy, HomeTabPresenter.SLOT_CONTENT);
    }


    @Override
    protected void onBind() {
        super.onBind();
    }
}
