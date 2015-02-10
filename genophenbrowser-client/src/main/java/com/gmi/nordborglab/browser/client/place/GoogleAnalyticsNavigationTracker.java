package com.gmi.nordborglab.browser.client.place;

import com.arcbees.analytics.shared.Analytics;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.NavigationHandler;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Created by uemit.seren on 2/10/15.
 */
public class GoogleAnalyticsNavigationTracker implements NavigationHandler {

    private final Analytics analytics;
    private final PlaceManager placeManager;

    @Inject
    public GoogleAnalyticsNavigationTracker(Analytics analytics, PlaceManager placeManager, EventBus eventBus) {
        this.analytics = analytics;
        this.placeManager = placeManager;
        eventBus.addHandler(NavigationEvent.getType(), this);

    }

    @Override
    public void onNavigation(NavigationEvent navigationEvent) {
        analytics.sendPageView().documentPath(placeManager.buildHistoryToken(navigationEvent.getRequest())).go();
    }
}
