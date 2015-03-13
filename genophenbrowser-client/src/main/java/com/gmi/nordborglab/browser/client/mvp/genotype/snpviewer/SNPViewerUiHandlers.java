package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created by uemit.seren on 3/3/15.
 */
public interface SNPViewerUiHandlers extends UiHandlers {
    void onSearchPhenotype(SuggestOracle.Request request, SuggestOracle.Callback callback);

    void onSelectRegion(String region);

    void onSelectPhenotype(SuggestOracle.Suggestion suggestion);

    void onSelectAlleleAssay(AlleleAssayProxy alleleAssay);

    void onSelectSNP(SNPInfoProxy snp);
}
