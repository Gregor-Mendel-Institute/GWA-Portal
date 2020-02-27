package com.gmi.nordborglab.browser.client.place;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.testing.CountingEventBus;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.TestEagerSingleton;
import org.jukito.TestSingleton;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Created by uemit.seren on 2/13/15.
 */
@RunWith(JukitoRunner.class)
public class GoogleAnalyticsNavigationTrackerTest {

    public static class Module extends JukitoModule {
        protected void configureTest() {
            bindMock(GoogleAnalyticsManager.class).in(TestSingleton.class);
            bind(EventBus.class).to(CountingEventBus.class).in(TestEagerSingleton.class);
            bind(CountingEventBus.class).in(TestEagerSingleton.class);
        }
    }

    @Inject
    private GoogleAnalyticsManager analyticsManager;

    @Inject
    private GoogleAnalyticsNavigationTracker sut;

    @Inject
    private CountingEventBus eventBus;

    @Test
    public void testRegisterEvent() {
        assertThat(eventBus.getHandlerCount(NavigationEvent.getType()), is(1));
    }

    @Test
    public void testOnNavigation() {
        PlaceRequest request = new PlaceRequest.Builder().nameToken("/place1/place2").build();
        sut.onNavigation(new NavigationEvent(request));
        verify(analyticsManager).sendPageView(request);
    }

}
