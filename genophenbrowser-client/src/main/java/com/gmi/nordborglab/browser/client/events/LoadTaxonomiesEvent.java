package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadTaxonomiesEvent extends GwtEvent<LoadTaxonomiesEvent.Handler> {


    public interface Handler extends EventHandler {
        public void onLoadTaxonomies(LoadTaxonomiesEvent event);
    }

    public static final Type<LoadTaxonomiesEvent.Handler> TYPE = new Type<LoadTaxonomiesEvent.Handler>();

    private final List<TaxonomyProxy> taxonomies;

    public LoadTaxonomiesEvent(List<TaxonomyProxy> taxonomies) {
        this.taxonomies = taxonomies;
    }

    @Override
    public Type<LoadTaxonomiesEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    public static Type<LoadTaxonomiesEvent.Handler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LoadTaxonomiesEvent.Handler handler) {
        handler.onLoadTaxonomies(this);
    }

    public static void fire(final HasHandlers source,
                            List<TaxonomyProxy> taxonomies) {
        source.fireEvent(new LoadTaxonomiesEvent(taxonomies));
    }

    public List<TaxonomyProxy> getTaxonomies() {
        return taxonomies;
    }
}
