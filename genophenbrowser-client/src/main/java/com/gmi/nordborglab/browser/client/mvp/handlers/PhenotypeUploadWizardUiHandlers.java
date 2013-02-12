package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/8/13
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PhenotypeUploadWizardUiHandlers  extends UiHandlers{

    void onUploadFinished(String responseText);

    void onUploadError(String responseText);

    void onCancel();

    void onCreate();
}
