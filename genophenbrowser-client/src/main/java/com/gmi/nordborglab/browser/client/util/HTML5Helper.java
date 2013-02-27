package com.gmi.nordborglab.browser.client.util;

import com.google.gwt.dom.client.DataTransfer;
import elemental.html.Blob;
import elemental.html.FileList;
import elemental.js.html.JsFormData;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class HTML5Helper {
    public static class ExtDataTransfer extends DataTransfer {

        protected ExtDataTransfer() {}
        public final native FileList getFiles() /*-{
            return this.files;
        }-*/;

    }

    public static class ExtJsFormData extends JsFormData {

        protected ExtJsFormData() {}

        public final native static ExtJsFormData newExtJsForm() /*-{
            return new $wnd.FormData();
        }-*/;

        public final native void append(String name,Blob file,String filename) /*-{
            this.append(name,file,filename);
        }-*/;

    }
}
