package com.gmi.nordborglab.browser.client.mvp.widgets.facets;

import com.gmi.nordborglab.browser.client.events.FacetSearchChangeEvent;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestBase;
import com.gmi.nordborglab.browser.client.testutils.PresenterTestModule;
import com.gmi.nordborglab.browser.client.testutils.RecordingAndCountingEventBus;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.testing.CountingEventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by uemit.seren on 2/11/15.
 */
public class FacetSearchPresenterWidgetTest extends PresenterTestBase {

    public static class Module extends PresenterTestModule {

        @Override
        protected void configurePresenterTest() {
        }
    }


    @Inject
    FacetSearchPresenterWidget presenter;


    @Inject
    FacetSearchPresenterWidget.MyView view;
    private final PlaceRequest request = new PlaceRequest.Builder().nameToken("test").build();


    @Before
    public void setUp() throws Exception {
        reset(view);
        MockitoAnnotations.initMocks(this);
        // required to have a default place
        given(placeManager.getCurrentPlaceRequest()).willReturn(request);
    }


    @Test
    public void testStandardMapAndDefaultFilter() {
        assertThat(presenter.getFilter(), is(notNullValue()));
        assertThat(presenter.getFilter(), is("PRIVATE"));
    }

    @Test
    public void testSetFilter() {
        presenter.setFilter("PUBLISHED");
        PlaceRequest newPlace = new PlaceRequest.Builder(request).with("filter", "PUBLISHED").build();
        verify(placeManager).revealPlace(newPlace);
    }

    @Test
    public void testCorrectFilterAndSearchStringAfterNavigation() {
        PlaceRequest newPlace = new PlaceRequest.Builder(request).with("filter", "PUBLISHED").with("query", "test").build();
        given(placeManager.getCurrentPlaceRequest()).willReturn(newPlace);
        presenter.onReset();
        assertThat(presenter.getFilter(), is("PUBLISHED"));
        assertThat(presenter.getSearchString(), is("test"));
        assertThat(((CountingEventBus) eventBus).getFiredCountFromSource(FacetSearchChangeEvent.TYPE, presenter), is(1));
        verify(view).setActiveNavLink("PUBLISHED");
        verify(view).setSearchString("test");
    }

    @Test
    public void testSetSearchBoxVisibility() {
        presenter.setSearchBoxVisible(false);
        verify(view).setSearchBoxVisible(false);
    }

    @Test
    public void testUpdateSearchString() {
        presenter.updateSearchString("test");
        PlaceRequest newPlace = new PlaceRequest.Builder(request).with("query", "test").build();
        verify(placeManager).revealPlace(newPlace);
    }

    @Test
    public void testIsFilterChangedInEvent() {
        PlaceRequest newPlace = new PlaceRequest.Builder(request).with("filter", "PUBLISHED").build();
        given(placeManager.getCurrentPlaceRequest()).willReturn(newPlace);
        given(placeManager.getCurrentPlaceRequest()).willReturn(newPlace);
        presenter.onReset();

        FacetSearchChangeEvent event = (FacetSearchChangeEvent) Iterables.getLast(((RecordingAndCountingEventBus) eventBus).getFiredEvents());
        assertThat(event.isSearchStringChanged(), is(false));
        assertThat(event.isFilterChanged(), is(true));
    }

    @Test
    public void testIsSearchStringChangedInEvent() {
        FacetSearchChangeEvent.Handler handler = mock(FacetSearchChangeEvent.Handler.class);
        PlaceRequest newPlace = new PlaceRequest.Builder(request).with("query", "test").build();
        given(placeManager.getCurrentPlaceRequest()).willReturn(newPlace);
        presenter.onReset();

        FacetSearchChangeEvent event = (FacetSearchChangeEvent) Iterables.getLast(((RecordingAndCountingEventBus) eventBus).getFiredEvents());
        assertThat(event.isSearchStringChanged(), is(true));
        assertThat(event.isFilterChanged(), is(false));
    }

    @Test
    public void testCustomDefaultFilter() {
        presenter.setDefaultFilter("ALL");
        assertThat(presenter.getFilter(), is("ALL"));
    }

    @Test
    public void testCustomFilterParam() {
        presenter.setDefaultFilterParam("custom_filter");
        presenter.setFilter("ALL");
        verify(placeManager).revealPlace(new PlaceRequest.Builder(request).with("custom_filter", "ALL").build());

    }

    @Test
    public void testCustomQuqeryParam() {
        presenter.setDefaultQueryParam("custom_query");
        presenter.updateSearchString("test");
        verify(placeManager).revealPlace(new PlaceRequest.Builder(request).with("custom_query", "test").build());
    }

    @Test
    public void testDynamicFacets() {
        presenter.initFixedFacets(null);
        verify(view, times(1)).clearFacets();
        verify(view, times(1)).setActiveNavLink(any(String.class));
        assertThat(presenter.isDynamic(), is(true));
    }

    @Test
    public void testChangeFixedFacets() {
        presenter.initFixedFacets(FacetSearchPresenterWidget.USER_MAP);
        verify(view, times(1)).clearFacets();
        verify(view, times(1)).setFacet(eq("ALL"), eq("All"), any(String.class));
        verify(view, times(1)).setFacet(eq("USER"), eq("Users"), any(String.class));
        verify(view, times(1)).setFacet(eq("ADMIN"), eq("Admins"), any(String.class));
        assertThat(presenter.isDynamic(), is(false));

    }

    @Test
    public void testDisplayFacets() {
        FacetProxy facet1 = mock(FacetProxy.class);
        FacetProxy facet2 = mock(FacetProxy.class);
        when(facet1.getTotal()).thenReturn(10L);
        when(facet1.getName()).thenReturn("ALL");
        when(facet2.getTotal()).thenReturn(5L);
        when(facet2.getName()).thenReturn("PRIVATE");
        List<FacetProxy> facets = Lists.newArrayList(facet1, facet2);
        presenter.displayFacets(facets);

        verify(view, never()).clearFacets();
        verify(view).setActiveNavLink("PRIVATE");
        verify(view, times(1)).setFacet(eq("ALL"), eq("All (10)"), any(String.class));
        verify(view, times(1)).setFacet(eq("PRIVATE"), eq("My items (5)"), any(String.class));
    }
}
