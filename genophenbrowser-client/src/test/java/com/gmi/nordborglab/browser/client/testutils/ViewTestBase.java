package com.gmi.nordborglab.browser.client.testutils;

import org.jukito.JukitoRunner;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

import com.google.gwt.junit.GWTMockUtilities;

@RunWith(JukitoRunner.class)
public class ViewTestBase {
	
	@AfterClass
	public static void tearDown() {
		GWTMockUtilities.restore();
	}

}
