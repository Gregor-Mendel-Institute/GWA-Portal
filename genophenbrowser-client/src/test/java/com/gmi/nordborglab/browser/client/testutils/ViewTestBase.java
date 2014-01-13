package com.gmi.nordborglab.browser.client.testutils;

import com.google.gwt.junit.GWTMockUtilities;
import org.jukito.JukitoRunner;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
public class ViewTestBase {

    @AfterClass
    public static void tearDown() {
        GWTMockUtilities.restore();
    }

}
