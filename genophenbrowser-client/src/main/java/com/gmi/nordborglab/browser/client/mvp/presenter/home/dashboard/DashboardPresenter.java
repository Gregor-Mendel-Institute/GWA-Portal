package com.gmi.nordborglab.browser.client.mvp.presenter.home.dashboard;

import com.gmi.nordborglab.browser.client.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.TabDataDynamic;
import com.gmi.nordborglab.browser.client.gin.ClientGinjector;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.HomeTabPresenter;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gmi.nordborglab.browser.client.mvp.presenter.home.HomePresenter;
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
    static TabData getTabLabel(ClientGinjector ginjector) {
        return new TabDataDynamic("Dashboard", 1, "",
                ginjector.getLoggedInGatekeeper());
    }


    @Inject
    public DashboardPresenter(final EventBus eventBus, final MyView view,
                              final MyProxy proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, HomeTabPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected void onBind() {
        super.onBind();
    }
}
