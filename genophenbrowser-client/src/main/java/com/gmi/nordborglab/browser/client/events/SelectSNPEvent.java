package com.gmi.nordborglab.browser.client.events;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created by uemit.seren on 10/21/14.
 */
public class SelectSNPEvent extends Event<SelectSNPEvent.Handler> {

    public interface Handler {
        public void onSelectSNP(SelectSNPEvent event);
    }

    protected final int chromosome;
    protected int xVal;
    protected int clientX;
    protected int clientY;

    public SelectSNPEvent(int chromosome, int xVal, int clientX, int clientY) {
        this.xVal = xVal;
        this.chromosome = chromosome;
        this.clientX = clientX;
        this.clientY = clientY;
    }

    public static final Type<Handler> TYPE = new Type<>();

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onSelectSNP(this);
    }

    public static HandlerRegistration register(EventBus eventBus, SelectSNPEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }


    public int getChromosome() {
        return chromosome;
    }

    public int getxVal() {
        return xVal;
    }

    public int getClientX() {
        return clientX;
    }

    public int getClientY() {
        return clientY;
    }
}
