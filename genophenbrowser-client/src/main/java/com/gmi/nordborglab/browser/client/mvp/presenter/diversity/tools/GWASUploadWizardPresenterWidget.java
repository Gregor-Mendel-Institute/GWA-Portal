package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.GWASUploadedEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.GWASUploadWizardUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASUploadWizardPresenterWidget extends PresenterWidget<GWASUploadWizardPresenterWidget.MyView> implements GWASUploadWizardUiHandlers{


    public interface MyView extends View,HasUiHandlers<GWASUploadWizardUiHandlers>{

        void showUploadPanel();

    }

    @Inject
    public GWASUploadWizardPresenterWidget(EventBus eventBus, MyView view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    @Override
    public void onUploadError(String responseText) {
        DisplayNotificationEvent.fireError(this,"Upload error",responseText);
        getView().showUploadPanel();
    }
    @Override
    public void onUploadFinished(String responseText) {
        Long id = Long.parseLong(responseText);
        getView().showUploadPanel();
        GWASUploadedEvent.fire(getEventBus(),id);
    }
}
