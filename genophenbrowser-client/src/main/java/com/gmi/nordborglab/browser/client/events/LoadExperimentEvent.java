package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.events.LoadExperimentEvent.LoadExperimentHandler;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class LoadExperimentEvent extends GwtEvent<LoadExperimentHandler> {

	public interface LoadExperimentHandler extends EventHandler {
		void onLoadExperiment(LoadExperimentEvent event);
	}
	
	public static final Type<LoadExperimentHandler> TYPE = new Type<LoadExperimentHandler>();
	
	private final ExperimentProxy experiment;
	
	public LoadExperimentEvent(ExperimentProxy experiment) {
		this.experiment = experiment;
	}

	@Override
	public Type<LoadExperimentHandler> getAssociatedType() {
		return TYPE;
	}
	
	public static Type<LoadExperimentHandler> getType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LoadExperimentHandler handler) {
		handler.onLoadExperiment(this);
	}
	
	public static void fire(final HasHandlers source,
			ExperimentProxy experiment) {
		source.fireEvent(new LoadExperimentEvent(experiment));
	}

	public ExperimentProxy getExperiment() {
		return experiment;
	}

}
