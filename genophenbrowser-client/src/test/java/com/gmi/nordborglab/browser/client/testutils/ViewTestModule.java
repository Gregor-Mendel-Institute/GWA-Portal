package com.gmi.nordborglab.browser.client.testutils;

import org.jukito.JukitoModule;

import com.google.gwt.junit.GWTMockUtilities;
import com.gwtplatform.tester.MockFactory;

public abstract class ViewTestModule extends JukitoModule {

	@Override
	protected void configureTest() {
		GWTMockUtilities.disarm();

	    bind(MockFactory.class).to(MockitoMockFactory.class);
	    
		configureViewTest();
	}
	
	protected abstract void configureViewTest();

}
