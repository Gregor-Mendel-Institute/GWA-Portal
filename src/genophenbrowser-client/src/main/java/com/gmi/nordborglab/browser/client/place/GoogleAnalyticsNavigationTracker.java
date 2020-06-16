package com.gmi.nordborglab.browser.client.place;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.NavigationHandler;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Created by uemit.seren on 2/10/15.
 */
public class GoogleAnalyticsNavigationTracker implements NavigationHandler {

    private final GoogleAnalyticsManager analyticsManager;
    private final PlaceManager placeManager;

    @Inject
    public GoogleAnalyticsNavigationTracker(GoogleAnalyticsManager analyticsManager, PlaceManager placeManager, EventBus eventBus) {
        this.analyticsManager = analyticsManager;
        this.placeManager = placeManager;
        eventBus.addHandler(NavigationEvent.getType(), this);

    }

    @Override
    public void onNavigation(NavigationEvent navigationEvent) {
        analyticsManager.sendPageView(navigationEvent.getRequest());
    }
}
