package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.google.web.bindery.event.shared.Event;

/**
 * Created by uemit.seren on 5/20/16.
 */
public class GWASDataLoadedEvent extends Event<GWASDataLoadedEvent.Handler> {

    public interface Handler {
        void onDataLoaded(GWASDataLoadedEvent event);
    }

    protected final GWASDataDTO gwasData;

    public GWASDataLoadedEvent(GWASDataDTO gwasData) {
        this.gwasData = gwasData;
    }

    public static final Type<Handler> TYPE = new Type<>();

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onDataLoaded(this);
    }

    public GWASDataDTO getGwasData() {
        return gwasData;
    }
}