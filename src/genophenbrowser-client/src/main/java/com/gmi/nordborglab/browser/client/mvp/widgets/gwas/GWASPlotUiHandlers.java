package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/26/13
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GWASPlotUiHandlers extends UiHandlers {

    void onSelectSNP(int chromosome, int xVal, int clientX, int clientY);

    void onLoadTrackData(String id, boolean isStacked, String chr);

    void onSearchGenes(String searchString, GWASPlotView.SearchGeneCallback callback);

    void onHighlightGene(String value, boolean isSelection);
}
