package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GWASUploadWizardUiHandlers extends UiHandlers{


    void onUploadError(String responseText);

    void onUploadFinished(String responseText);
}
