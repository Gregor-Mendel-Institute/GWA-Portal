package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.ontology.TraitOntologyPresenter;
import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/7/13
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyLoadedEvent  extends Event<OntologyLoadedEvent.Handler> {


    public interface Handler extends EventHandler {
        void onOntologyLoaded(OntologyLoadedEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final TermProxy term;

    public OntologyLoadedEvent(TermProxy term) {
        this.term = term;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onOntologyLoaded(this);
    }

    public static HandlerRegistration register(EventBus eventBus,OntologyLoadedEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source,
                            TermProxy term) {
        source.fireEvent(new OntologyLoadedEvent(term));
    }


    public TermProxy getTerm() {
        return term;
    }
}