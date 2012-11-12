package com.gmi.nordborglab.browser.client.events;

import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent.LoadingIndicatorHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class LoadingIndicatorEvent  extends GwtEvent<LoadingIndicatorHandler> {
	
	private static final Type<LoadingIndicatorHandler> TYPE = new Type<LoadingIndicatorHandler>();
	protected boolean show;
	protected String text = "Loading...";
	
	public interface LoadingIndicatorHandler extends EventHandler {
		  void onProcessLoadingIndicator(LoadingIndicatorEvent event);
	}
	
	public static Type<LoadingIndicatorHandler> getType() {
		return TYPE;
	}

	 public static void fire(HasHandlers source,boolean show) {
		 source.fireEvent(new LoadingIndicatorEvent(show));
	 }
	 
	 public static void fire(HasHandlers source, boolean show,String text) {
		 source.fireEvent(new LoadingIndicatorEvent(show,text));
	 }
		  
	 public LoadingIndicatorEvent(boolean show) {
		 this(show,null);
	 }
	 
	 public LoadingIndicatorEvent(boolean show,String text) {
		 this.show = show;
		 if (text != null && !text.equals(""))
			 this.text = text; 
	 }

	  @Override
	  protected void dispatch(LoadingIndicatorHandler handler) {
	    handler.onProcessLoadingIndicator(this);
	  }

	  @Override
	  public Type<LoadingIndicatorHandler> getAssociatedType() {
	    return getType();
	  }
	  
	  public boolean getShow()
	  {
		  return this.show;
	  }
	  
	  public String getText() {
		  return text;
	  }
}

