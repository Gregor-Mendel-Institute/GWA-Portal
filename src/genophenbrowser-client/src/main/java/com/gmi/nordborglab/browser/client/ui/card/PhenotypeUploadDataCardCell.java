package com.gmi.nordborglab.browser.client.ui.card;

import com.gmi.nordborglab.browser.client.util.SearchTerm;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;

/**
 * Created by uemit.seren on 6/30/14.
 */
public class PhenotypeUploadDataCardCell extends AbstractCell<PhenotypeUploadDataProxy> {


    private final PhenotypeUploadDataCardRenderer uiRenderer;
    private final SearchTerm searchTerm;

    @Inject
    public PhenotypeUploadDataCardCell(SearchTerm searchTerm, PhenotypeUploadDataCardRenderer uiRenderer) {
        super();
        this.searchTerm = searchTerm;
        this.uiRenderer = uiRenderer;

    }

    @Override
    public void render(Cell.Context context, PhenotypeUploadDataProxy value, SafeHtmlBuilder sb) {
        if (value == null)
            return;
        uiRenderer.render(searchTerm, value, sb);
    }

    public SearchTerm getSearchTerm() {
        return searchTerm;
    }
}
