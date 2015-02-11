package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 2:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GWASViewerUiHandlers extends UiHandlers{
    void onShowPermissions(GWASResultProxy object);

    void onEdit(GWASResultProxy object);

    void onDelete(GWASResultProxy object);

    void cancelEdits();

    void saveEdits();
}
