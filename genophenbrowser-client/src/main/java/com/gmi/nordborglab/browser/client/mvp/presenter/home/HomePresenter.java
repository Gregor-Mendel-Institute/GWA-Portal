package com.gmi.nordborglab.browser.client.mvp.presenter.home;

import com.gmi.nordborglab.browser.client.mvp.handlers.HomeUiHandlers;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.shared.proxy.AppStatProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramFacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import com.gmi.nordborglab.browser.shared.proxy.NewsItemProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePresenter extends
        Presenter<HomePresenter.MyView, HomePresenter.MyProxy> implements HomeUiHandlers {

    public interface MyView extends View, HasUiHandlers<HomeUiHandlers> {

        void setLinkToWizard(boolean isLoggedIn);

        void setStatValue(AppStatProxy.STAT stat, long value);

        HasData<NewsItemProxy> getNewsDisplay();

        void displayHistogram(List<DateStatHistogramProxy> dateStatHistogramProxies, DateStatHistogramFacetProxy.TYPE currentRecentPublishedType, DateStatHistogramProxy.INTERVAL currentHistInterval);

    }

    @ProxyStandard
    @NameToken(NameTokens.home)
    @TabInfo(label = "Home", priority = 0, container = HomeTabPresenter.class)
    public interface MyProxy extends TabContentProxyPlace<HomePresenter> {

    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    private final CurrentUser currentUser;

    private final ListDataProvider<NewsItemProxy> newsDataProvider = new ListDataProvider<NewsItemProxy>();
    private final CustomRequestFactory rf;
    private DateStatHistogramProxy.INTERVAL currentHistInterval = DateStatHistogramProxy.INTERVAL.MONTH;
    private Map<DateStatHistogramFacetProxy.TYPE, List<DateStatHistogramProxy>> recentPublishedHistogram;
    private DateStatHistogramFacetProxy.TYPE currentRecentPublishedType = DateStatHistogramFacetProxy.TYPE.phenotype;

    @Inject
    public HomePresenter(final EventBus eventBus, final MyView view,
                         final MyProxy proxy, final CurrentUser currentUser, final CustomRequestFactory rf) {
        super(eventBus, view, proxy, HomeTabPresenter.TYPE_SetTabContent);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.currentUser = currentUser;
        newsDataProvider.addDataDisplay(getView().getNewsDisplay());
    }


    @Override
    protected void onReset() {
        super.onReset();
        if (currentUser.isLoggedIn()) {
            getView().setLinkToWizard(true);
        } else {
            getView().setLinkToWizard(false);
        }
        displayData();
        fetchHistogram();
    }

    @Override
    public void onChangeChartType(DateStatHistogramFacetProxy.TYPE type) {
        currentRecentPublishedType = type;
        fetchHistogram();
    }

    @Override
    public void onChangeChartInterval(DateStatHistogramProxy.INTERVAL interval) {
        if (currentHistInterval != interval) {
            currentHistInterval = interval;
            recentPublishedHistogram = null;
            fetchHistogram();
        }

    }

    private void displayHistogram() {
        if (recentPublishedHistogram == null)
            return;
        getView().displayHistogram(recentPublishedHistogram.get(currentRecentPublishedType), currentRecentPublishedType, currentHistInterval);
    }

    private void fetchHistogram() {
        if (recentPublishedHistogram == null) {
            rf.helperRequest().findRecentTraitHistogram(currentHistInterval).fire(new Receiver<List<DateStatHistogramFacetProxy>>() {
                @Override
                public void onSuccess(List<DateStatHistogramFacetProxy> response) {
                    recentPublishedHistogram = new HashMap<DateStatHistogramFacetProxy.TYPE, List<DateStatHistogramProxy>>();
                    for (DateStatHistogramFacetProxy facet : response) {
                        recentPublishedHistogram.put(facet.getType(), facet.getHistogram());
                    }
                    displayHistogram();
                }
            });
        } else {
            displayHistogram();
        }
    }

    private void displayData() {
        for (AppStatProxy stat : currentUser.getAppData().getStats()) {
            getView().setStatValue(stat.getStat(), stat.getValue());
        }
        newsDataProvider.setList(currentUser.getAppData().getNews());
    }

    @Override
    protected void onBind() {
        super.onBind();
    }
}
