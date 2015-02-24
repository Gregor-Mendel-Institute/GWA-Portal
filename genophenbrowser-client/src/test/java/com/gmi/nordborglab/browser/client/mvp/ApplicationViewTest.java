package com.gmi.nordborglab.browser.client.mvp;

import com.gmi.nordborglab.browser.client.mvp.ApplicationPresenter.MENU;
import com.gmi.nordborglab.browser.client.mvp.ApplicationView.Binder;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.testutils.ViewTestBase;
import com.gmi.nordborglab.browser.client.testutils.ViewTestModule;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.tester.MockFactory;
import com.gwtplatform.tester.MockingBinder;
import org.jukito.All;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class ApplicationViewTest extends ViewTestBase {

    public static class Module extends ViewTestModule {

        static class MyTestBinder extends MockingBinder<Widget, ApplicationView> implements Binder {
            @Inject
            public MyTestBinder(final MockFactory mockitoMockFactory) {
                super(Widget.class, mockitoMockFactory);
            }
        }

        @Override
        protected void configureViewTest() {
            bind(Binder.class).to(MyTestBinder.class);
            bindManyInstances(MENU.class, MENU.HOME, MENU.DIVERSITY, MENU.GERMPLASM, MENU.GENOTYPE);
        }
    }

    @Inject
    ApplicationView view;


    @Test
    public void testSetActiveNavigationItem(@All MENU activeItem) {
        String active_style_name = "ACTIVE_STYLE_NAME";
        given(view.style.current_page_item()).willReturn(active_style_name);
        given(view.homeLink.getTargetHistoryToken()).willReturn(NameTokens.home);
        given(view.diversityLink.getTargetHistoryToken()).willReturn(NameTokens.experiments);
        view.setActiveNavigationItem(activeItem);
        verify(view.homeLink).removeStyleName(active_style_name);
        verify(view.diversityLink).removeStyleName(active_style_name);

        if (activeItem.equals(NameTokens.home))
            verify(view.homeLink).addStyleName(active_style_name);
        else if (activeItem.equals(NameTokens.experiments))
            verify(view.diversityLink).addStyleName(active_style_name);
        else
            fail(activeItem + " unknown");
    }


}
