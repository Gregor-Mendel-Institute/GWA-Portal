package com.gmi.nordborglab.browser.client.mvp.home;

import com.gmi.nordborglab.browser.client.mvp.ApplicationPresenter;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 24.06.13
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public class HomeTabPresenter extends TabContainerPresenter<HomeTabPresenter.MyView, HomeTabPresenter.MyProxy> {

    public interface MyView extends TabView {
    }

    @RequestTabs
    public static final GwtEvent.Type<RequestTabsHandler> TYPE_RequestTabs = new GwtEvent.Type<RequestTabsHandler>();

    @ChangeTab
    public static final GwtEvent.Type<ChangeTabHandler> TYPE_ChangeTab = new GwtEvent.Type<ChangeTabHandler>();


    public static final NestedSlot SLOT_CONTENT = new NestedSlot();

    @ProxyStandard
    public interface MyProxy extends Proxy<HomeTabPresenter> {
    }

    @Inject
    public HomeTabPresenter(final EventBus eventBus, final MyView view,
                            final MyProxy proxy) {
        super(eventBus, view, proxy, SLOT_CONTENT, TYPE_RequestTabs, TYPE_ChangeTab, ApplicationPresenter.SLOT_MAIN_CONTENT);
    }
}
