package com.gmi.nordborglab.browser.client.place;

import com.arcbees.analytics.shared.Analytics;
import com.arcbees.analytics.shared.options.EventsOptions;
import com.arcbees.analytics.shared.options.TimingOptions;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.List;

/**
 * Created by uemit.seren on 2/12/15.
 */
public class GoogleAnalyticsManager {

    final Analytics analytics;
    final PlaceManager placeManager;
    final CurrentUser currentUser;

    @Inject
    public GoogleAnalyticsManager(Analytics analytics, PlaceManager placeManager, CurrentUser currentUser) {
        this.analytics = analytics;
        this.placeManager = placeManager;
        this.currentUser = currentUser;
    }


    public void sendPageView(PlaceRequest request) {
        analytics.sendPageView().documentPath(getURLFromRequest(request)).go();
    }

    public void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, null, false);
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

    public void sendError(String action, String message, boolean isFatal) {
        sendError(action, message, true, true, isFatal);
    }

    public void sendError(String action, String message, boolean includePlace, boolean includeUser, boolean isFatal) {
        List<String> messages = Lists.newArrayList();
        if (includePlace) {
            messages.add("Place: " + getURLFromRequest());
        }
        if (includeUser) {
            messages.add("User:" + getCurrentUser());
        }
        messages.add("Exception: " + message);
        String errorMessage = Joiner.on(", ").join(messages);
        sendException(errorMessage, isFatal);
        sendEvent("Errors", action, errorMessage, true);
    }

    private String getCurrentUser() {
        String user = "annonymous";
        if (currentUser.isLoggedIn()) {
            user = String.valueOf(currentUser.getUserId());
            if (currentUser.isLoggedIn()) {
                user += " (Admin)";
            }
        }
        return user;
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
