package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/22/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenotypeCard extends CellWidget<AlleleAssayProxy> {

    @Inject
    public GenotypeCard(GenotypeCardCell cell) {
        super(cell);
        this.getElement().getStyle().setFloat(Style.Float.LEFT);
        this.getElement().getStyle().setMarginRight(30, Style.Unit.PX);
    }
}
