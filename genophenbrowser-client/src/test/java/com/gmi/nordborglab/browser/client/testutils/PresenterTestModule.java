package com.gmi.nordborglab.browser.client.testutils;

import org.jukito.JukitoModule;
import org.jukito.TestSingleton;

import com.gmi.nordborglab.browser.client.ClientPlaceManager;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.gin.DefaultPlace;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.gwt.junit.GWTMockUtilities;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.server.ServiceLayer;
import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.server.SimpleRequestProcessor;
import com.google.web.bindery.requestfactory.server.testing.InProcessRequestTransport;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;
import com.google.web.bindery.requestfactory.vm.RequestFactorySource;
import com.gwtplatform.mvp.client.AutobindDisable;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

public abstract class PresenterTestModule extends JukitoModule {
	

	@Override
	protected void configureTest() {
		GWTMockUtilities.disarm();
	    bind(EventBus.class).to(CountingEventBus.class).in(TestSingleton.class);
	    configurePresenterTest();
	    bind(AutobindDisable.class).toInstance(new AutobindDisable(true));
	}
	
	abstract protected void configurePresenterTest();

	@Provides
	@Singleton
	public CustomRequestFactory createCustomRequestFactory(EventBus eventBus,ServiceLayerDecorator serviceLayerDecorator) {
		SimpleRequestProcessor processor = new SimpleRequestProcessor(ServiceLayer.create(serviceLayerDecorator));
		CustomRequestFactory factory = RequestFactorySource.create( CustomRequestFactory.class);
	    factory.initialize( eventBus, new InProcessRequestTransport( processor ) );
		return factory;
	}
	
}
