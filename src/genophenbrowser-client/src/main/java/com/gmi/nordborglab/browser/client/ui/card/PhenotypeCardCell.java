package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/22/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeCardCell extends AbstractCell<PhenotypeProxy> {


    private final SearchTerm searchTerm;
    private final PhenotypeCardRenderer uiRenderer;

    @Inject
    public PhenotypeCardCell(SearchTerm searchTerm, PhenotypeCardRenderer uiRenderer) {
        super();
        this.searchTerm = searchTerm;
        this.uiRenderer = uiRenderer;

    }

    @Override
    public void render(Context context, PhenotypeProxy value, SafeHtmlBuilder sb) {
        if (value == null)
            return;
        uiRenderer.render(searchTerm, value, sb);
    }

    public SearchTerm getSearchTerm() {
        return searchTerm;
    }
}
