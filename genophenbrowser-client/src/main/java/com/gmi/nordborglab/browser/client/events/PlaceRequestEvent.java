package com.gmi.nordborglab.browser.client.events;

import java.util.Map;

import com.gmi.nordborglab.browser.client.events.PlaceRequestEvent.PlaceRequestHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class PlaceRequestEvent extends GwtEvent<PlaceRequestHandler> {

	public interface PlaceRequestHandler extends EventHandler {
		void onChangePlaceRequest(PlaceRequestEvent event);
	}

	public static final Type<PlaceRequestHandler> TYPE = new Type<PlaceRequestHandler>(); 
	private final Map<String,String> paramters;
	
	public PlaceRequestEvent(final Map<String,String> paramters ) {
		this.paramters = paramters;
	}
	
	public static Type<PlaceRequestHandler> getType() {
		return TYPE;
	}

	@Override
	public Type<PlaceRequestHandler> getAssociatedType() {
		return TYPE;
	}

	public static void fire(final HasHandlers source,
			Map<String,String> paramters) {
		source.fireEvent(new PlaceRequestEvent(paramters));
	}

	@Override
	protected void dispatch(PlaceRequestHandler handler) {
		handler.onChangePlaceRequest(this);
	}
	
	public Map<String,String> getParameters() {
		return paramters;
	}
	

}
