package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/22/13
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeCard extends CellWidget<PhenotypeProxy> {


    @Inject
    public PhenotypeCard(PhenotypeCardCell cell) {
        super(cell);
        this.getElement().getStyle().setFloat(Style.Float.LEFT);
        this.getElement().getStyle().setMarginRight(30, Style.Unit.PX);
    }
}
