package com.gmi.nordborglab.browser.client.mvp.widgets.facets;

import com.gmi.nordborglab.browser.client.testutils.ViewTestBase;
import com.google.gwt.core.shared.GWT;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.WithClassesToStub;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Created by uemit.seren on 2/11/15.
 */
@WithClassesToStub({Anchor.class})
public class FacetSearchPresenterWidgetViewTest extends ViewTestBase {

    FacetSearchPresenterWidgetView view;

    @GwtMock
    AnchorListItem navLink;

    @Before
    public void setUp() {
        view = new FacetSearchPresenterWidgetView(GWT.<FacetSearchPresenterWidgetView.Binder>create(FacetSearchPresenterWidgetView.Binder.class));
    }




    @Test
    public void testClearFacets() {
        view.clearFacets();
        verify(view.navPills, times(1)).clear();
        assertThat(view.navLinkMap.size(), is(0));
    }

    @Test
    public void testSetSearchBoxVisible() {
        view.setSearchBoxVisible(false);
        verify(view.searchBoxContainer, times(1)).setVisible(false);
    }

    @Test
    public void testSetSearchString() {
        view.setSearchString("test");
        verify(view.searchBox, times(1)).setText("test");
    }

    @Test
    public void testSetActiveLinkNoFacets() {
        view.setActiveNavLink("ALL");
        verify(navLink, never()).setActive(anyBoolean());
    }

    @Test
    public void testSetFacetsCreateNonExisting() {
        view.setFacet("ALL", "All", "link");
        assertThat(view.navLinkMap.size(), is(1));
        AnchorListItem link = view.navLinkMap.get("ALL");
        verify(view.navPills, times(1)).add(link);
        // TODO check if link has correct values
    }

    @Test
    public void testSetFacetsWithExisting() {
        view.navLinkMap.put("ALL", new AnchorListItem());
        view.setFacet("ALL", "All", "link");
        view.setFacet("PRIVATE", "Private", "link");
        assertThat(view.navLinkMap.size(), is(2));
        AnchorListItem link = view.navLinkMap.get("PRIVATE");
        verify(view.navPills, times(1)).add(link);
    }

}
