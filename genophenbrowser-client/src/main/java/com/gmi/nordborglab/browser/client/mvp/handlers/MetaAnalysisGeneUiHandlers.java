package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.MetaSNPAnalysisProxy;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MetaAnalysisGeneUiHandlers extends UiHandlers {

    void onSearchForGene(SuggestOracle.Request request, SuggestOracle.Callback callback);

    void onSelectGene(SuggestOracle.Suggestion suggestion);

    void onSelectMetaAnalysis(MetaSNPAnalysisProxy metaAnalysis);

    void onChangeRange(int lowerLimit, int upperLimit);
}
