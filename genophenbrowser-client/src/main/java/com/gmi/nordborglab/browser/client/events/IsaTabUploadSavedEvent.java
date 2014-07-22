package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created by uemit.seren on 7/18/14.
 */
public class IsaTabUploadSavedEvent extends Event<IsaTabUploadSavedEvent.Handler> {


    public interface Handler extends EventHandler {
        void onSave(IsaTabUploadSavedEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final ExperimentProxy experiment;

    public IsaTabUploadSavedEvent(ExperimentProxy experiment) {
        this.experiment = experiment;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onSave(this);
    }

    public static HandlerRegistration register(EventBus eventBus, IsaTabUploadSavedEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

    public static void fire(final EventBus source,
                            ExperimentProxy experiment) {
        source.fireEvent(new IsaTabUploadSavedEvent(experiment));
    }


    public ExperimentProxy getExperiment() {
        return experiment;
    }
}
