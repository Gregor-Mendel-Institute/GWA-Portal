package com.gmi.nordborglab.browser.client.mvp.widgets.gwas;

import com.gmi.nordborglab.browser.client.events.GWASUploadedEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
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
public class GWASUploadWizardPresenterWidget extends PresenterWidget<GWASUploadWizardPresenterWidget.MyView> implements GWASUploadWizardUiHandlers {


    private boolean multipleUpload = true;
    private String restURL = "provider/gwas/upload";
    private final GoogleAnalyticsManager analyticsManager;

    public boolean isMultipleUpload() {
        return multipleUpload;
    }

    public interface MyView extends View, HasUiHandlers<GWASUploadWizardUiHandlers> {

        void setmultipleUpload(boolean multipleUpload);

        void setRestURL(String restUrl);
    }

    @Inject
    public GWASUploadWizardPresenterWidget(EventBus eventBus, MyView view, final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view);
        this.analyticsManager = analyticsManager;
        getView().setUiHandlers(this);
    }

    @Override
    public void onClose() {
        GWASUploadedEvent.fire(getEventBus(), 0L);
    }

    @Override
    public void onUploadStart() {
        analyticsManager.startTimingEvent("GWAS", "Upload");
        fireEvent(new LoadingIndicatorEvent(true, "Uploading..."));
    }

    @Override
    public void onUploadEnd() {
        fireEvent(new LoadingIndicatorEvent(false));
        analyticsManager.endTimingEvent("GWAS", "Upload", "OK");
        analyticsManager.sendEvent("GWAS", "Upload", "URL:" + this.restURL);
    }

    @Override
    public void onUploadError(String errorMessage) {
        fireEvent(new LoadingIndicatorEvent(false));
        analyticsManager.endTimingEvent("GWAS", "Upload", "ERROR");
        analyticsManager.sendError("GWAS", "URL:" + this.restURL + ",Error:" + errorMessage, true);
    }

    public void setMultipleUpload(boolean multipleUpload) {
        this.multipleUpload = multipleUpload;
        getView().setmultipleUpload(multipleUpload);
    }

    public void setRestURL(String restUrl) {
        this.restURL = restUrl;
        getView().setRestURL(restUrl);
    }
}
