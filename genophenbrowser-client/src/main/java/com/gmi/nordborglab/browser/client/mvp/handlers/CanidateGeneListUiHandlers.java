package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.CandidateGeneListProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 20.09.13
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public interface CanidateGeneListUiHandlers extends UiHandlers {

    void updateSearchString(String value);

    void onSave();

    void onCancel();

    void onCreate();
}
