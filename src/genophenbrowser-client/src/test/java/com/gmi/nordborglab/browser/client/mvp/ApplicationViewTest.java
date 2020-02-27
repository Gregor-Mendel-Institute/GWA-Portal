package com.gmi.nordborglab.browser.client.mvp;

import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.client.testutils.ViewTestBase;
import com.gmi.nordborglab.browser.client.ui.favicon.FavicoOptions;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.WithClassesToStub;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WithClassesToStub({FavicoOptions.class})
public class ApplicationViewTest extends ViewTestBase {

    ApplicationView view;

    @Mock
    PlaceManager placeManager;
    @GwtMock
    Style style;

    @Before
    public void setUp() {
        view = new ApplicationView(GWT.create(ApplicationView.Binder.class), GWT.create(MainResources.class), placeManager);
        given(view.footerContentPanel.getStyle()).willReturn(style);
    }

    @Test
    public void testHideFooterWhenNotHome() {
        view.setActiveNavigationItem(ApplicationPresenter.MENU.DIVERSITY);
        verify(view.mainContainer).setWidgetSize(view.footerPanel, 0.5);
        verify(style).setDisplay(Style.Display.NONE);
    }

    @Test
    public void testShowFooterWhenHome() {
        view.setActiveNavigationItem(ApplicationPresenter.MENU.HOME);
        verify(view.mainContainer).setWidgetSize(view.footerPanel, 4.423);
        verify(style).setDisplay(Style.Display.BLOCK);
    }


}
