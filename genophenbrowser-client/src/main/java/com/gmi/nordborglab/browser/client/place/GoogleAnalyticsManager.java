package com.gmi.nordborglab.browser.client.place;

import com.arcbees.analytics.shared.Analytics;
import com.arcbees.analytics.shared.options.EventsOptions;
import com.arcbees.analytics.shared.options.TimingOptions;
import com.gmi.nordborglab.browser.client.events.GoogleAnalyticsEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Created by uemit.seren on 2/12/15.
 */
public class GoogleAnalyticsManager {

    final Analytics analytics;
    final PlaceManager placeManager;

    @Inject
    public GoogleAnalyticsManager(Analytics analytics, PlaceManager placeManager) {
        this.analytics = analytics;
        this.placeManager = placeManager;
    }


    public void sendPageView(PlaceRequest request) {
        analytics.sendPageView().documentPath(getURLFromRequest(request)).go();
    }

    public void sendEvent(GoogleAnalyticsEvent event, boolean isNonInteractive) {
        GoogleAnalyticsEvent.GAEventData data = event.getEventData();
        sendEvent(data.getCategory(), data.getAction(), data.getLabel(), data.getValue(), isNonInteractive);
    }

    public void sendEvent(String category, String action, String label, boolean isNonInteractive) {
        sendEvent(category, action, label, null, isNonInteractive);
    }

    public void sendEvent(String category, String action, String label, Integer value, boolean isNonInteractive) {
        sendEvent(category, action, label, value, isNonInteractive, null);
    }

    public void sendEvent(String category, String action, String label, Integer value, boolean isNonInteractive, PlaceRequest request) {
        EventsOptions event = analytics.sendEvent(category, action).eventLabel(label);
        if (value != null)
            event.eventValue(value);
        event.hitOptions().nonInteractionHit(isNonInteractive)
                .contentOptions().documentPath(getURLFromRequest(request)).go();
    }

    public void setLoggedIn(boolean loggedIn) {
        analytics.setGlobalSettings().customsOptions().customDimension(1, loggedIn ? "YES" : "NO").go();
    }

    private String getURLFromRequest(PlaceRequest request) {
        if (request == null)
            request = placeManager.getCurrentPlaceRequest();
        if (request != null) {
            return placeManager.buildHistoryToken(request);
        }
        return null;
    }


    private String getURLFromRequest() {
        return getURLFromRequest(null);
    }

    public void sendException(String exception, boolean isFatal) {
        analytics.sendException(exception).isExceptionFatal(isFatal).contentOptions().documentPath(getURLFromRequest()).go();

    }

    public void startTimingEvent(String category, String var) {
        analytics.startTimingEvent(category, var);
    }

    public void endTimingEvent(String category, String var) {
        endTimingEvent(category, var, null);
    }

    public void endTimingEvent(String category, String var, String label) {
        TimingOptions timing = analytics.endTimingEvent(category, var);
        if (label != null)
            timing.userTimingLabel(label);
        timing.contentOptions().documentPath(getURLFromRequest()).go();
    }
}
