package com.gmi.nordborglab.browser.client.testutils;

import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.AutobindDisable;
import org.jukito.JukitoModule;
import org.jukito.TestSingleton;

public abstract class PresenterTestModule extends JukitoModule {


    @Override
    protected void configureTest() {
        GWTMockUtilities.disarm();
        bind(EventBus.class).to(RecordingAndCountingEventBus.class).in(TestSingleton.class);
        configurePresenterTest();
        bind(AutobindDisable.class).toInstance(new AutobindDisable(true));
        bindMock(CustomRequestFactory.class).in(TestSingleton.class);
    }

    abstract protected void configurePresenterTest();

}
