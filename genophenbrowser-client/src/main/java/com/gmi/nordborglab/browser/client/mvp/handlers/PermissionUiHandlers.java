package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.client.ui.CustomMultiWordSuggestOracle.MultiWordSuggestion;
import com.gwtplatform.mvp.client.UiHandlers;

public interface PermissionUiHandlers extends UiHandlers {

	public void onSave();

	public void onAddPermission(MultiWordSuggestion selectedItem);
}
