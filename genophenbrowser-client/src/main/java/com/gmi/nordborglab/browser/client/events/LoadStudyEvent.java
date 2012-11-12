package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.events.LoadStudyEvent.LoadStudyHandler;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class LoadStudyEvent extends GwtEvent<LoadStudyHandler> {

	public interface LoadStudyHandler extends EventHandler {
		void onLoad(LoadStudyEvent event);
	}
	
	public static final Type<LoadStudyHandler> TYPE = new Type<LoadStudyHandler>();
	
	private final StudyProxy study;
	
	public LoadStudyEvent(StudyProxy study) {
		this.study = study;
	}

	@Override
	public Type<LoadStudyHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<LoadStudyHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LoadStudyHandler handler) {
		handler.onLoad(this);
	}
	
	public static void fire(final HasHandlers source,
			StudyProxy study) {
		source.fireEvent(new LoadStudyEvent(study));
	}

	public StudyProxy getStudy() {
		return study;
	}

}
