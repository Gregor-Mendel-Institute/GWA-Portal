package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.client.ui.OntologyTermSuggestOracle;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

public interface PhenotypeDetailUiHandlers extends UiHandlers {

    public void onEdit();

    public void onSave();

    public void onCancel();

    public void onDelete();

    void onConfirmDelete();

    void onSelectStatisticType(StatisticTypeProxy statisticType);

    void onSearchOntology(SuggestOracle.Request request, SuggestOracle.Callback callback, ConstEnums.ONTOLOGY_TYPE type);
}
