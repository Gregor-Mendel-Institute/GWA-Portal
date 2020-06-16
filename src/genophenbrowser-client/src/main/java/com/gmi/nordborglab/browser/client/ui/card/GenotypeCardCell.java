package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/22/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenotypeCardCell extends AbstractCell<AlleleAssayProxy> {


    protected final GenotypeCardRenderer genotypeCardRenderer;

    @Inject
    public GenotypeCardCell(GenotypeCardRenderer genotypeCardRenderer) {
        super();
        this.genotypeCardRenderer = genotypeCardRenderer;
    }

    @Override
    public void render(Context context, AlleleAssayProxy value, SafeHtmlBuilder sb) {
        genotypeCardRenderer.render(value,sb);
    }
}
