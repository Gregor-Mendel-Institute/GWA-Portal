package com.gmi.nordborglab.browser.client.events;


import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import org.gwtbootstrap3.client.ui.constants.AlertType;


public class DisplayNotificationEvent extends Event<DisplayNotificationEvent.DisplayNotificationHandler> {

	  
	public interface DisplayNotificationHandler extends EventHandler {
		  void onDisplayNotifcation(DisplayNotificationEvent event);
	}
	
	public static final int DURATION_PERMANENT = 0;
	public static final int DURATION_SHORT = 5000;
	public static final int DURATION_NORMAL = 10000;
	public static final int DURATION_LONG = 20000;

	private static final Type<DisplayNotificationHandler> TYPE = new Type<DisplayNotificationHandler>();

	public static Type<DisplayNotificationHandler> getType() {
		return TYPE;
	}


    public static void fire(EventBus source, String caption, String message, boolean dismissable, AlertType alertType, int duration) {
        source.fireEvent(new DisplayNotificationEvent(caption, message, dismissable, alertType, duration));
    }


    public static void fireMessage(EventBus source, String caption, String message) {
        fire(source, caption, message, true, AlertType.DEFAULT, DURATION_NORMAL);
    }


    public static void fireClearMessage(HasHandlers source) {
		//fire(source, null, false, LEVEL_MESSAGE, DURATION_PERMANENT);
	}


    public static void fireError(EventBus source, String caption, String message) {
        fire(source, caption, message, true, AlertType.DANGER, DURATION_PERMANENT);
    }

    public static void fireWarning(EventBus source, String caption, String message) {
        fire(source, caption, message, true, AlertType.WARNING, DURATION_NORMAL);
    }



	private final String caption;
	private final String message;
	private final boolean dismissable;
    private final AlertType alertType;
    private final int duration;

	private boolean alreadyDisplayed;


    public DisplayNotificationEvent(String caption, String message, boolean dismissable, AlertType alertType, int duration) {
        this.caption = caption;
		this.message = message;
		this.dismissable = dismissable;
        this.alertType = alertType;
        this.duration = duration;
	}


    public String getMessage() {
		return message;
	}


    public boolean isDismissable() {
		return dismissable;
	}


    public AlertType getAlertType() {
        return alertType;
    }


    public int getDuration() {
		return duration;
	}


    public boolean isAlreadyDisplayed() {
		return alreadyDisplayed;
	}


    public void setAlreadyDisplayed() {
		// Null messages should clear every UI, so don't set the flag.
		if (message != null) {
			alreadyDisplayed = true;
		}
	}

	@Override
	protected void dispatch(DisplayNotificationHandler handler) {
		handler.onDisplayNotifcation(this);
	}

	@Override
	public Type<DisplayNotificationHandler> getAssociatedType() {
		return getType();
	}

	public String getCaption() {
		return caption;
	}

}
