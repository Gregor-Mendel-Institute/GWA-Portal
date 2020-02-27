package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.events.LoadProxyEvent.LoadProxyHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public abstract class LoadProxyEvent<T> extends GwtEvent<LoadProxyHandler<T>> {

	public interface LoadProxyHandler<T> extends EventHandler {
		void onLoad(LoadProxyEvent<T> event);
	}
	
	private final T proxy;
	
	public LoadProxyEvent(T proxy) {
		this.proxy = proxy;
	}

	@Override
	protected void dispatch(LoadProxyHandler<T> handler) {
		handler.onLoad(this);
	}

	public T getProxy() {
		return proxy;
	}
	
	
}
