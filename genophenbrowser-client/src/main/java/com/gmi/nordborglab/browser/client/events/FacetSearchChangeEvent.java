package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

/**
 * Created by uemit.seren on 2/10/15.
 */
public class FacetSearchChangeEvent extends Event<FacetSearchChangeEvent.Handler> {

    private final boolean isFilterChanged;
    private final boolean isSearchStringChanged;

    public interface Handler extends EventHandler {
        void onChanged(FacetSearchChangeEvent event);
    }

    public static final Event.Type<Handler> TYPE = new Event.Type<>();

    public FacetSearchChangeEvent(boolean isFilterChanged, boolean isSearchStringChanged) {

        this.isFilterChanged = isFilterChanged;
        this.isSearchStringChanged = isSearchStringChanged;
    }

    @Override
    public Event.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onChanged(this);
    }

    public boolean isFilterChanged() {
        return isFilterChanged;
    }

    public boolean isSearchStringChanged() {
        return isSearchStringChanged;
    }
}