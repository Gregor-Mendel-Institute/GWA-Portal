package com.gmi.nordborglab.browser.client.mvp.widgets.facets;

import com.gmi.nordborglab.browser.client.testutils.ViewTestBase;
import com.gmi.nordborglab.browser.client.testutils.ViewTestModule;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.tester.MockFactory;
import com.gwtplatform.tester.MockingBinder;
import org.junit.Test;

/**
 * Created by uemit.seren on 2/11/15.
 */
public class FacetSearchPresenterWidgetViewTest extends ViewTestBase {


    public static class Module extends ViewTestModule {

        static class MyTestBinder extends MockingBinder<Widget, FacetSearchPresenterWidgetView> implements FacetSearchPresenterWidgetView.Binder {
            @Inject
            public MyTestBinder(final MockFactory mockitoMockFactory) {
                super(Widget.class, mockitoMockFactory);
            }
        }

        @Override
        protected void configureViewTest() {
            bind(FacetSearchPresenterWidgetView.Binder.class).to(MyTestBinder.class);
        }
    }

    @Inject
    FacetSearchPresenterWidgetView view;


    @Test
    public void testSetActiveNavigationItem() {

    }


}
