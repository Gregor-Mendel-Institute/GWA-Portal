package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/19/13
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadedEvent extends Event<PhenotypeUploadedEvent.Handler> {


    public interface Handler extends EventHandler {
        void onPhenotypeUploaded(PhenotypeUploadedEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final ExperimentProxy experiment;

    public PhenotypeUploadedEvent(ExperimentProxy experiment) {
        this.experiment = experiment;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onPhenotypeUploaded(this);
    }

    public static HandlerRegistration register(EventBus eventBus, PhenotypeUploadedEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

    public static void fire(final EventBus source,
                            ExperimentProxy experiment) {
        source.fireEvent(new PhenotypeUploadedEvent(experiment));
    }


    public ExperimentProxy getExperiment() {
        return experiment;
    }
}
