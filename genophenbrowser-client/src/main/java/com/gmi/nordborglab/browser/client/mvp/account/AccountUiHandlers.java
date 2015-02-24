package com.gmi.nordborglab.browser.client.mvp.account;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 30.10.13
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public interface AccountUiHandlers extends UiHandlers {
    void onSave();

    void onCancel();

    boolean onValidate();

    void onSelectAvatarSource(AppUserProxy.AVATAR_SOURCE gravatar);
}
