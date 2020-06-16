package com.gmi.nordborglab.browser.client.testutils;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.testing.CountingEventBus;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by uemit.seren on 2/11/15.
 */
public class RecordingAndCountingEventBus extends CountingEventBus {

    private final Set<Event<?>> firedEvents = new HashSet<Event<?>>();

    @Override
    public void fireEventFromSource(Event<?> event, Object source) {
        firedEvents.add(event);
        super.fireEventFromSource(event, source);
    }

    @Override
    public void fireEvent(Event<?> event) {
        firedEvents.add(event);
        super.fireEvent(event);
    }

    public Set<Event<?>> getFiredEvents() {
        return firedEvents;
    }
}
