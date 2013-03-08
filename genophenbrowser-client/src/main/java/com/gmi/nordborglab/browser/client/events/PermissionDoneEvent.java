package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/2/13
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionDoneEvent extends Event<PermissionDoneEvent.Handler> {

    public interface Handler extends EventHandler {
        void onPermissionDone(PermissionDoneEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();


    public PermissionDoneEvent() {

    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onPermissionDone(this);
    }

    public static HandlerRegistration register(EventBus eventBus,PermissionDoneEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source) {
        source.fireEvent(new PermissionDoneEvent());
    }
}
