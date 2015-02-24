package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 08.10.13
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public interface FilterItemPresenterUiHandlers extends UiHandlers {

    void onOpenFilterSettings();

    void onCancel();

    void onAdd();

    void onSearchByQuery(SuggestOracle.Request request, SuggestOracle.Callback callback);
}
