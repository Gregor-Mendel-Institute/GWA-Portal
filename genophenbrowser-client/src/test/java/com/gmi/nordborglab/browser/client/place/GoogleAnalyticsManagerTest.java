package com.gmi.nordborglab.browser.client.place;

import com.arcbees.analytics.shared.Analytics;
import com.arcbees.analytics.shared.options.ContentOptions;
import com.arcbees.analytics.shared.options.CustomsOptions;
import com.arcbees.analytics.shared.options.EventsOptions;
import com.arcbees.analytics.shared.options.ExceptionOptions;
import com.arcbees.analytics.shared.options.HitOptions;
import com.arcbees.analytics.shared.options.TimingOptions;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.TestSingleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by uemit.seren on 2/12/15.
 */


@RunWith(JukitoRunner.class)
public class GoogleAnalyticsManagerTest {

    public static class Module extends JukitoModule {
        protected void configureTest() {
            // required because of builder pattern
            bindMock(CurrentUser.class).in(TestSingleton.class);
            Analytics analyticsMock = mock(Analytics.class, RETURNS_DEEP_STUBS);
            bind(Analytics.class).toInstance(analyticsMock);
        }
    }

    private static final String place = "/place1/place2";
    private PlaceRequest request = new PlaceRequest.Builder().nameToken(place).build();

    @Inject
    PlaceManager placeManager;

    @Inject
    Analytics analytics;

    @Inject
    CurrentUser currentUser;


    ContentOptions contentOptions;


    @Inject
    GoogleAnalyticsManager sut;

    @Before
    public void setup() {
        given(placeManager.getCurrentPlaceRequest()).willReturn(request);
        given(placeManager.buildHistoryToken(request)).willReturn(place);
        reset(analytics);
        contentOptions = mock(ContentOptions.class);
        given(contentOptions.documentPath(anyString())).willReturn(contentOptions);
    }

    @Test
    public void testSetLoggedInTrue() {
        testSetLoggedIn(true);
    }

    @Test
    public void testSetLoggedInFalse() {
        testSetLoggedIn(false);
    }

    private void testSetLoggedIn(boolean loggedIn) {
        CustomsOptions customOptions = mock(CustomsOptions.class, RETURNS_DEEP_STUBS);
        //required because of builder
        given(customOptions.customDimension(any(Integer.class), any(String.class))).willReturn(customOptions);
        given(analytics.setGlobalSettings().customsOptions()).willReturn(customOptions);
        sut.setLoggedIn(loggedIn);
        verify(customOptions).customDimension(1, loggedIn ? "YES" : "NO");
        // required because otherwise Nullpointer exception
        verify(customOptions).go();
    }

    @Test
    public void testStartTimingEvent() {
        sut.startTimingEvent("category", "var");
        verify(analytics).startTimingEvent("category", "var");
    }


    @Test
    public void testEndTimingEventWithLabel() {
        testEndTimingEvent("label");
    }

    @Test
    public void testEndTimingEventWithOutLabel() {
        testEndTimingEvent(null);
    }

    private void testEndTimingEvent(String label) {
        TimingOptions options = mock(TimingOptions.class, RETURNS_DEEP_STUBS);
        given(analytics.endTimingEvent(any(String.class), any(String.class))).willReturn(options);
        given(options.contentOptions()).willReturn(contentOptions);
        sut.endTimingEvent("category", "var", label);
        verify(analytics).endTimingEvent("category", "var");
        if (label != null)
            verify(options).userTimingLabel(label);
        verifyContentOptions();
    }

    @Test
    public void testSendExceptionFatal() {
        testSendException(true);
    }

    @Test
    public void testSendExceptionNonFatal() {
        testSendException(false);
    }


    private void testSendException(boolean isfatal) {
        ExceptionOptions exceptionOptions = mock(ExceptionOptions.class, RETURNS_DEEP_STUBS);
        setupExceptionTest(exceptionOptions);
        sut.sendException("exception", isfatal);
        verify(analytics).sendException("exception");
        verify(exceptionOptions).isExceptionFatal(isfatal);
        verifyContentOptions();
    }


    @Test
    public void testSendPageView() {
        given(analytics.sendPageView()).willReturn(contentOptions);
        sut.sendPageView(request);
        verify(analytics, atLeastOnce()).sendPageView();
        verifyContentOptions(place);
    }

    @Test
    public void testSendEventWithValue() {
        testSendEvent(10, null);
    }

    @Test
    public void testSendEventWithoutValue() {
        testSendEvent(null, null);
    }

    @Test
    public void testSendEventWithCustomPlace() {
        testSendEvent(null, "/custom/custom");
    }


    @Test
    public void testSendErrorDefault() {
        testSendError(true, true, true);
    }

    @Test
    public void testSendErrorDefault_WithOutPlaceAndUser() {
        testSendError(false, false, true);
    }

    @Test
    public void testSendErrorDefault_WithOnlyPlace() {
        testSendError(true, false, true);
    }

    @Test
    public void testSendErrorDefault_WithOnlyUser() {
        testSendError(false, true, true);
    }

    @Test
    public void testSendErrorDefault_AsAdmin() {
        testSendError(true, true, true, true);
    }

    private void testSendError(boolean includePlace, boolean includeUser, boolean isFatal) {
        testSendError(includePlace, includeUser, isFatal, false);
    }

    private void testSendError(boolean includePlace, boolean includeUser, boolean isFatal, boolean isAdmin) {
        if (isAdmin) {
            given(currentUser.isLoggedIn()).willReturn(true);
            given(currentUser.isAdmin()).willReturn(true);
            given(currentUser.getUserId()).willReturn(1);
        } else {
            given(currentUser.isLoggedIn()).willReturn(false);
        }
        EventsOptions eventOptions = mock(EventsOptions.class);
        HitOptions hitOptions = mock(HitOptions.class);
        ExceptionOptions exceptionOptions = mock(ExceptionOptions.class);
        setupExceptionTest(exceptionOptions);
        setUpEventTest(eventOptions, hitOptions);
        sut.sendError("testError", "testMessage", includePlace, includeUser, isFatal);
        verifyErrorSent(eventOptions, hitOptions, exceptionOptions, includePlace, includeUser, isFatal, isAdmin);
    }

    private void testSendEvent(Integer value, String customPlace) {
        PlaceRequest customRequest = null;
        if (customPlace != null) {
            customRequest = new PlaceRequest.Builder().nameToken(customPlace).build();
        }
        EventsOptions eventOptions = mock(EventsOptions.class);
        HitOptions hitOptions = mock(HitOptions.class);
        given(placeManager.buildHistoryToken(customRequest)).willReturn(customPlace);
        setUpEventTest(eventOptions, hitOptions);

        sut.sendEvent("category", "action", "label", value, true, customRequest);

        verifyEventSent(eventOptions, hitOptions, value, customPlace);
    }

    private void setUpEventTest(EventsOptions eventOptions, HitOptions hitOptions) {

        given(eventOptions.hitOptions()).willReturn(hitOptions);
        given(eventOptions.eventLabel(anyString())).willReturn(eventOptions);
        given(eventOptions.eventValue(anyInt())).willReturn(eventOptions);
        given(hitOptions.nonInteractionHit(anyBoolean())).willReturn(hitOptions);
        given(analytics.sendEvent(anyString(), anyString())).willReturn(eventOptions);
        given(hitOptions.contentOptions()).willReturn(contentOptions);
    }

    private void setupExceptionTest(ExceptionOptions exceptionOptions) {
        given(analytics.sendException(anyString())).willReturn(exceptionOptions);
        given(exceptionOptions.contentOptions()).willReturn(contentOptions);
        given(exceptionOptions.isExceptionFatal(anyBoolean())).willReturn(exceptionOptions);
    }

    private void verifyErrorSent(EventsOptions eventOptions, HitOptions hitOptions, ExceptionOptions exceptionOptions, boolean includePlace, boolean includeUser, boolean isFatal, boolean isAdmin) {
        verify(analytics).sendEvent("Errors", "testError");
        List<String> messages = Lists.newArrayList();
        if (includePlace) {
            messages.add("Place: " + GoogleAnalyticsManagerTest.place);
        }
        if (includeUser) {
            if (!isAdmin) {
                messages.add("User:annonymous");
            } else {
                messages.add("User:1 (Admin)");
            }
        }
        messages.add("Exception: testMessage");
        String errorMessage = Joiner.on(", ").join(messages);
        verify(eventOptions).eventLabel(errorMessage);
        verify(analytics).sendException(errorMessage);
        verify(exceptionOptions).isExceptionFatal(isFatal);
        verify(contentOptions, times(2)).documentPath(GoogleAnalyticsManagerTest.place);
        verify(contentOptions.documentPath(GoogleAnalyticsManagerTest.place), times(2)).go();
    }

    private void verifyEventSent(EventsOptions eventOptions, HitOptions hitOptions, Integer value, String customPlace) {
        verify(analytics).sendEvent("category", "action");
        verify(eventOptions).eventLabel("label");
        if (value != null)
            verify(eventOptions).eventValue(value);
        verify(eventOptions.hitOptions()).nonInteractionHit(true);
        if (customPlace != null)
            verifyContentOptions(customPlace);
        else
            verifyContentOptions();
    }


    private void verifyContentOptions() {
        verifyContentOptions(place);
    }

    private void verifyContentOptions(String place) {
        verify(contentOptions).documentPath(place);
        verify(contentOptions.documentPath(place)).go();
    }

}
