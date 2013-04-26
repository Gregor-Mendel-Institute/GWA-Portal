package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/22/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadUserNotificationEvent extends Event<LoadUserNotificationEvent.Handler> {

    public interface Handler extends EventHandler {
        void onLoaduserNotifications(LoadUserNotificationEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();


    public LoadUserNotificationEvent() {
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onLoaduserNotifications(this);
    }



}
