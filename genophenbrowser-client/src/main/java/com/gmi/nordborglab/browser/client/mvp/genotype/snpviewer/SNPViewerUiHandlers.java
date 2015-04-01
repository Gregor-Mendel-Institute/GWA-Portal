package com.gmi.nordborglab.browser.client.mvp.genotype.snpviewer;

import com.gmi.nordborglab.browser.client.manager.SearchManager;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created by uemit.seren on 3/3/15.
 */
public interface SNPViewerUiHandlers extends UiHandlers {
    void onSearchPhenotype(String request, SearchManager.SearchCallback callback);

    void onSelectRegion(String region);

    void onSelectPhenotype(SearchItemProxy phenotype);

    void onSelectAlleleAssay(AlleleAssayProxy alleleAssay);

    void onSelectSNP(SNPInfoProxy snp);
}
