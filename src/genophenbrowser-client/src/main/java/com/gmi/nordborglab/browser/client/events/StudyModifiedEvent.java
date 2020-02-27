package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/11/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class StudyModifiedEvent extends Event<StudyModifiedEvent.Handler> {


    public interface Handler extends EventHandler {
        void onStudyModified(StudyModifiedEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final StudyProxy study;

    public StudyModifiedEvent(StudyProxy study) {
        this.study = study;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onStudyModified(this);
    }

    public static HandlerRegistration register(EventBus eventBus,StudyModifiedEvent.Handler handler) {
        return eventBus.addHandler(TYPE,handler);
    }

    public static void fire(final EventBus source,
                            StudyProxy study) {
        source.fireEvent(new StudyModifiedEvent(study));
    }


    public StudyProxy getStudy() {
        return study;
    }
}