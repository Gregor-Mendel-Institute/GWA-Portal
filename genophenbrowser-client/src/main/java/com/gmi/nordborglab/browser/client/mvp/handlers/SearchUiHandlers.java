package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle.SearchSuggestion;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

public interface SearchUiHandlers extends UiHandlers {

	public void onRequestSearch(SuggestOracle.Request request,SuggestOracle.Callback callback);

	public void onNavigateToSuggestion(SearchSuggestion searchSuggestion);
}
