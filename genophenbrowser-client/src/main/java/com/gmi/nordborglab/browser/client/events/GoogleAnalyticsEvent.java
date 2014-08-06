package com.gmi.nordborglab.browser.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created by uemit.seren on 7/18/14.
 */
public class GoogleAnalyticsEvent extends Event<GoogleAnalyticsEvent.Handler> {


    public interface Handler extends EventHandler {
        void onTrack(GoogleAnalyticsEvent event);
    }

    public static final Type<Handler> TYPE = new Type<Handler>();

    private final GAEventData event;

    public GoogleAnalyticsEvent(GAEventData event) {
        this.event = event;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onTrack(this);
    }

    public static HandlerRegistration register(EventBus eventBus, GoogleAnalyticsEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

    public static void fire(final EventBus source,
                            GAEventData event) {
        source.fireEvent(new GoogleAnalyticsEvent(event));
    }


    public GAEventData getEventData() {
        return event;
    }

    /**
     * Created by uemit.seren on 8/6/14.
     */
    public static class GAEventData {

        private final String category;
        private final String action;
        private final String label;
        private final Integer value;
        private final boolean noninteraction;


        public GAEventData(String category, String action, String label) {
            this(category, action, label, null, false);
        }

        public GAEventData(String category, String action, String label, Integer value) {
            this(category, action, label, value, false);
        }

        public GAEventData(String category, String action, String label, Integer value, boolean noninteraction) {
            this.category = category;
            this.action = action;
            this.label = label;
            this.value = value;
            this.noninteraction = noninteraction;
        }


        public String getCategory() {
            return category;
        }

        public String getAction() {
            return action;
        }

        public String getLabel() {
            return label;
        }

        public Integer getValue() {
            return value;
        }

        public boolean isNoninteraction() {
            return noninteraction;
        }
    }
}
