package com.gmi.nordborglab.browser.client.mvp.profile;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.10.13
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
public interface ProfileUiHandlers extends UiHandlers {
    void onChangeType(ProfilePresenter.TYPE study);
}
