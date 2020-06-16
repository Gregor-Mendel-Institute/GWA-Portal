package com.gmi.nordborglab.browser.client.ui.cells;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 20.06.13
 * Time: 14:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class PlaceManagerColumn<T, C> extends Column<T, C> {
    protected final PlaceManager placeManager;

    public PlaceManagerColumn(Cell<C> cell, PlaceManager placeManager) {
        super(cell);
        this.placeManager = placeManager;
    }
}

