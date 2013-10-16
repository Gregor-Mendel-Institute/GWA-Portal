package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.dto.FilterItem;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 14.10.13
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
public interface FilterPresenterUiHandlers extends UiHandlers {
    void removeFilter(FilterItem filterItem);
}
