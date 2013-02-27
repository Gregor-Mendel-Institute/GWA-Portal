package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASUploadedEvent extends Event<GWASUploadedEvent.Handler> {


    public interface Handler extends EventHandler {
        void onGWASUploaded(GWASUploadedEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final Long id;

    public GWASUploadedEvent(Long id) {
        this.id = id;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onGWASUploaded(this);
    }

    public static HandlerRegistration register(EventBus eventBus,GWASUploadedEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source,
                            Long id) {
        source.fireEvent(new GWASUploadedEvent(id));
    }


    public Long getId() {
        return id;
    }
}
