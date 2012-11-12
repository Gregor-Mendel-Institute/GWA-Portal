package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.events.LoadPhenotypeEvent.LoadPhenotypeHandler;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class LoadPhenotypeEvent extends GwtEvent<LoadPhenotypeHandler> {

	public interface LoadPhenotypeHandler extends EventHandler {
		void onLoad(LoadPhenotypeEvent event);
	}
	
	public static final Type<LoadPhenotypeHandler> TYPE = new Type<LoadPhenotypeHandler>();
	
	private final PhenotypeProxy phenotype;
	
	public LoadPhenotypeEvent(PhenotypeProxy phenotype) {
		this.phenotype = phenotype;
	}

	@Override
	public Type<LoadPhenotypeHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<LoadPhenotypeHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LoadPhenotypeHandler handler) {
		handler.onLoad(this);
	}
	
	public static void fire(final HasHandlers source,
			PhenotypeProxy phenotype) {
		source.fireEvent(new LoadPhenotypeEvent(phenotype));
	}

	public PhenotypeProxy getPhenotype() {
		return phenotype;
	}

}
