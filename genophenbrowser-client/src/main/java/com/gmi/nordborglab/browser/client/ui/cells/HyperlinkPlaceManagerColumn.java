package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.Cell;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 20.06.13
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
public abstract class HyperlinkPlaceManagerColumn<T> extends PlaceManagerColumn<T, HyperlinkPlaceManagerColumn.HyperlinkParam> {


    public static class HyperlinkParam {
        protected final String name;
        protected final String url;

        public HyperlinkParam(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

    public HyperlinkPlaceManagerColumn(Cell<HyperlinkParam> cell, PlaceManager placeManager) {
        super(cell, placeManager);
    }
}