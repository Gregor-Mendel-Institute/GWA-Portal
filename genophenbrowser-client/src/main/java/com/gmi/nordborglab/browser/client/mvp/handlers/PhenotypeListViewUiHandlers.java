package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 29.07.13
 * Time: 14:20
 * To change this template use File | Settings | File Templates.
 */
public interface PhenotypeListViewUiHandlers extends UiHandlers {

    void selectFilter(ConstEnums.TABLE_FILTER filter);

    void updateSearchString(String searchString);
}
