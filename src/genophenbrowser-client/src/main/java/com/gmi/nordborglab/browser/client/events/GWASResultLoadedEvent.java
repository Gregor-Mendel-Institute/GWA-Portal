package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/27/13
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASResultLoadedEvent extends Event<GWASResultLoadedEvent.Handler> {

    public interface Handler extends EventHandler {
        void onGWASResultLoaded(GWASResultLoadedEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final GWASResultProxy gwasResult;

    public GWASResultLoadedEvent(GWASResultProxy gwasResult) {
        this.gwasResult = gwasResult;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onGWASResultLoaded(this);
    }

    public static HandlerRegistration register(EventBus eventBus,GWASResultLoadedEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source,
                            GWASResultProxy gwasResult) {
        source.fireEvent(new GWASResultLoadedEvent(gwasResult));
    }


    public GWASResultProxy getGWASResult() {
        return gwasResult;
    }
}
