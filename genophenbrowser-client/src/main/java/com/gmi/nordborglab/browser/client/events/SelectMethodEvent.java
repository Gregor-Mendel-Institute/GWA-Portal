package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.ui.card.MethodCard;
import com.gmi.nordborglab.browser.client.ui.card.TransformationCard;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectMethodEvent extends Event<SelectMethodEvent.Handler>{


    public interface Handler {
        public void onSelectMethod(SelectMethodEvent event);
    }
    public static final Type<Handler> TYPE = new Type<Handler>();

    protected final MethodCard card;

    public SelectMethodEvent(final MethodCard card) {
        this.card = card;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onSelectMethod(this);
    }

    public static HandlerRegistration register(EventBus eventBus,SelectMethodEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source,
                            MethodCard card) {
        if (source == null)
            return;
        source.fireEvent(new SelectMethodEvent(card));
    }

    public MethodCard getCard() {
        return card;
    }
}
