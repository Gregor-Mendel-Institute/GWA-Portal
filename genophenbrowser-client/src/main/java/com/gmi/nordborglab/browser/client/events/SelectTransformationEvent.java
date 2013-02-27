package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.ui.card.TransformationCard;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectTransformationEvent extends Event<SelectTransformationEvent.Handler> {


    public interface Handler {
        public void onSelectTransformation(SelectTransformationEvent event);
    }

    protected TransformationCard transformationCard;

    public SelectTransformationEvent(TransformationCard transformationCard) {
        this.transformationCard = transformationCard;
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    @Override
    public Type<Handler> getAssociatedType() {
       return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onSelectTransformation(this);
    }

    public static HandlerRegistration register(EventBus eventBus,SelectTransformationEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source,
                            TransformationCard transformationCard) {
        if (source == null)
            return;
        source.fireEvent(new SelectTransformationEvent(transformationCard));
    }

    public TransformationCard getTransformationCard() {
        return transformationCard;
    }
}
