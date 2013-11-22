package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 07.11.13
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public interface UserListUiHandler extends UiHandlers {
    void updateSearchString(String value);

    void selectFilter(ConstEnums.USER_FILTER filter);
}
