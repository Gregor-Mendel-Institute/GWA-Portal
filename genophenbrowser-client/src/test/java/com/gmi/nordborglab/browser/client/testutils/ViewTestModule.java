package com.gmi.nordborglab.browser.client.testutils;

import com.google.gwt.junit.GWTMockUtilities;
import com.gwtplatform.tester.MockFactory;
import org.jukito.JukitoModule;

public abstract class ViewTestModule extends JukitoModule {

    @Override
    protected void configureTest() {
        GWTMockUtilities.disarm();

        bind(MockFactory.class).to(MockitoMockFactory.class);

        configureViewTest();
    }

    protected abstract void configureViewTest();

}
