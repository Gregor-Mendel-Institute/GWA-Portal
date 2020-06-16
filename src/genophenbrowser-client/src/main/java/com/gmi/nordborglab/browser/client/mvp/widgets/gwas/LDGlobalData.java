package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by uemit.seren on 5/13/16.
 */

public class LDGlobalData extends JavaScriptObject {


    protected LDGlobalData() {
    }

    public final native LDGlobal getGlobalData(String chr) /*-{
        return this[chr];
    }-*/;
}
