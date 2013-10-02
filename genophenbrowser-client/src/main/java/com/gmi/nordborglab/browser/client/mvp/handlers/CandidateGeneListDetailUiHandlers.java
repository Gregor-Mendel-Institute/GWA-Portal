package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.09.13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public interface CandidateGeneListDetailUiHandlers extends UiHandlers {

    void onSearchForGene(SuggestOracle.Request request, SuggestOracle.Callback callback);

    void onSelectGene(SuggestOracle.Suggestion suggestion);

    void onAddGene();

    void selectFilter(ConstEnums.GENE_FILTER gene_filter);

    void onEdit();

    void onDelete();

    void onConfirmDelete();

    void onCancel();

    void onSave();

    void onShare();

    void onDeleteGene(GeneProxy gene);
}
