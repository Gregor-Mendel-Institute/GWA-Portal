package com.gmi.nordborglab.browser.client.gin;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.gwtplatform.dispatch.client.actionhandler.DefaultClientActionHandlerRegistry;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;


public class ClientDispatchModule extends AbstractGinModule {

	@Override
	protected void configure() {
		bind(DefaultClientActionHandlerRegistry.class).to(ClientActionHandlerRegistry.class).in(Singleton.class);
    	install(new  DispatchAsyncModule());
		
	}

}
