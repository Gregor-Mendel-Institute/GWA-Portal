package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 05.11.13
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class UserChangeEvent extends Event<UserChangeEvent.Handler> {

    public interface Handler extends EventHandler {
        void onChanged(UserChangeEvent event);
    }

    public static final Event.Type<Handler> TYPE = new Event.Type<Handler>();

    private final AppUserProxy user;

    public UserChangeEvent(final AppUserProxy user) {
        this.user = user;
    }

    @Override
    public Event.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    public AppUserProxy getUser() {
        return user;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onChanged(this);
    }


}