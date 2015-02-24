package com.gmi.nordborglab.browser.client.mvp.germplasm.taxonomy.list;

import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TaxonomyOverviewUiHandlers extends UiHandlers {

    void onClickTaxonomy(TaxonomyProxy taxonomy);
}
