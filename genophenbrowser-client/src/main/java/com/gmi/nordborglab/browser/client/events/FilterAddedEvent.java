package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.Event;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 10:47
 * To change this template use File | Settings | File Templates.
 */
public class FilterAddedEvent extends Event<FilterAddedEvent.Handler> {

    public interface Handler extends EventHandler {
        void onFilterAdded(FilterAddedEvent event);
    }

    public static final Event.Type<Handler> TYPE = new Event.Type<Handler>();


    public FilterAddedEvent() {
    }

    @Override
    public Event.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onFilterAdded(this);
    }


}
