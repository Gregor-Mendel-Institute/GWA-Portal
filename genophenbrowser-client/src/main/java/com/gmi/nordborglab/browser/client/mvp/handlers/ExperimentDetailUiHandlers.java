package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.gwtplatform.mvp.client.UiHandlers;

public interface ExperimentDetailUiHandlers extends UiHandlers {
    public void onEdit();

    public void onSave();

    public void onCancel();

    public void onDelete();

    void onShare();

    void onDeletePublication(PublicationProxy publication);

    void queryDOI(String DOI);

    void onConfirmDelete();
}
