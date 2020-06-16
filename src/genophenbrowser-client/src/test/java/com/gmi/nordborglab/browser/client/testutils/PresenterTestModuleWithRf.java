package com.gmi.nordborglab.browser.client.testutils;

import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.server.ServiceLayer;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.server.SimpleRequestProcessor;
import com.google.web.bindery.requestfactory.server.testing.InProcessRequestTransport;
import com.google.web.bindery.requestfactory.vm.RequestFactorySource;

/**
 * Created by uemit.seren on 3/3/15.
 */
public abstract class PresenterTestModuleWithRf extends PresenterTestModule {

    @Provides
    @Singleton
    public CustomRequestFactory createCustomRequestFactory(EventBus eventBus, ServiceLayerDecorator serviceLayerDecorator) {
        SimpleRequestProcessor processor = new SimpleRequestProcessor(ServiceLayer.create(serviceLayerDecorator));
        CustomRequestFactory factory = RequestFactorySource.create(CustomRequestFactory.class);
        factory.initialize(eventBus, new InProcessRequestTransport(processor));
        return factory;
    }
}
