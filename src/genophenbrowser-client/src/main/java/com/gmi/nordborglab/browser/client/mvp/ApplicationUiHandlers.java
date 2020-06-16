package com.gmi.nordborglab.browser.client.mvp;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/19/13
 * Time: 11:10 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ApplicationUiHandlers extends UiHandlers {
    public void onOpenAccountInfo();

    void onCloseAccountInfo();
}
