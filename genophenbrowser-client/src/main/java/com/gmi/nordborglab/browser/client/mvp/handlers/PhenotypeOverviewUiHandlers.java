package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 05.07.13
 * Time: 14:30
 * To change this template use File | Settings | File Templates.
 */
public interface PhenotypeOverviewUiHandlers extends UiHandlers {
    void selectFilter(ConstEnums.TABLE_FILTER filter);

    void updateSearchString(String searchString);
}
