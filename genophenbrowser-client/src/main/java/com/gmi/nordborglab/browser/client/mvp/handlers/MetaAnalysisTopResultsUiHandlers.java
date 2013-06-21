package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.meta.MetaAnalysisTopResultsPresenter;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public interface MetaAnalysisTopResultsUiHandlers extends UiHandlers {

    void onChangeSelections(MetaAnalysisTopResultsPresenter.STATS stat, Integer row);
}
