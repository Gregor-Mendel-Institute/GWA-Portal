package com.gmi.nordborglab.browser.client.ui.favicon;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 18.10.13
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
public class Favico {

    private final FavicoPeer peer;

    public Favico(FavicoOptions options) {
        peer = FavicoPeer.create(options);
    }

    public void badge(int number) {
        peer.badge(number);
    }

    public void reset() {
        peer.reset();
    }


    private static class FavicoPeer extends JavaScriptObject {

        protected FavicoPeer() {
        }

        public static native FavicoPeer create(FavicoOptions options)/*-{
            return new $wnd.Favico(options);
        }-*/;

        public final native void badge(int number)/*-{
            this.badge(number);
        }-*/;

        public final native void reset()/*-{
            this.reset();
        }-*/;
    }

}
