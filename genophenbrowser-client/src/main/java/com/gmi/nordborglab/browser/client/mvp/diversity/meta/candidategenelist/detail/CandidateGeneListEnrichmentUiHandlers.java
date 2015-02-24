package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.12.13
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public interface CandidateGeneListEnrichmentUiHandlers extends UiHandlers {
    void selectVisibleRecords(Boolean select);

    void selectAllRecords(boolean clearSelection);

    void onRunEnrichment();
}
