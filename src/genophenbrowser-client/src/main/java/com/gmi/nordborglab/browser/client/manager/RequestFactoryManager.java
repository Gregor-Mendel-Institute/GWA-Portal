package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.RequestContext;

public abstract class RequestFactoryManager<T extends RequestContext> {

	protected final CustomRequestFactory rf;
	
	@Inject
	public RequestFactoryManager(final CustomRequestFactory rf) {
		this.rf = rf;
	}
	
	public CustomRequestFactory requestFactory() {
		return rf;
	}
	
	public abstract T getContext();
}
