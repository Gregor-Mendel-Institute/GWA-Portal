package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.events.LoadTaxonomyEvent.LoadTaxonomyHandler;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class LoadTaxonomyEvent extends GwtEvent<LoadTaxonomyHandler> {

	public interface LoadTaxonomyHandler extends EventHandler {
		void onLoad(LoadTaxonomyEvent event);
	}
	
	public static final Type<LoadTaxonomyHandler> TYPE = new Type<LoadTaxonomyHandler>();
	
	private final TaxonomyProxy taxonomy;
	
	public LoadTaxonomyEvent(TaxonomyProxy taxonomy) {
		this.taxonomy = taxonomy;
	}

	@Override
	public Type<LoadTaxonomyHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<LoadTaxonomyHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LoadTaxonomyHandler handler) {
		handler.onLoad(this);
	}
	
	public static void fire(final HasHandlers source,
			PhenotypeProxy phenotype) {
		source.fireEvent(new LoadPhenotypeEvent(phenotype));
	}

	public TaxonomyProxy getTaxonomy() {
		return taxonomy;
	}

}