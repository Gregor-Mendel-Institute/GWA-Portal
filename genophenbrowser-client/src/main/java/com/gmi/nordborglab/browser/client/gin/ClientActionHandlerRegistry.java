package com.gmi.nordborglab.browser.client.gin;

import com.gmi.nordborglab.browser.client.dispatch.command.GetGWASDataActionHandler;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.client.actionhandler.DefaultClientActionHandlerRegistry;

public class ClientActionHandlerRegistry extends
		DefaultClientActionHandlerRegistry {
	
	@Inject
	public ClientActionHandlerRegistry(GetGWASDataActionHandler getGWASDataActionHandler
											) {
		register(getGWASDataActionHandler);
	}
	

}
