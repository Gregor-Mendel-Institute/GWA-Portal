package com.gmi.nordborglab.browser.client.testutils;

import org.jukito.JukitoRunner;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

@RunWith(JukitoRunner.class)
public class PresenterTestBase {

	@Inject
	protected PlaceManager placeManager;

	@Inject
	protected EventBus eventBus;

	@AfterClass
	public static void tearDown() {
		GWTMockUtilities.restore();
	}

}
