package com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools;

import com.arcbees.analytics.shared.Analytics;
import com.gmi.nordborglab.browser.client.events.GWASUploadedEvent;
import com.gmi.nordborglab.browser.client.events.GoogleAnalyticsEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
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
public class GWASUploadWizardPresenterWidget extends PresenterWidget<GWASUploadWizardPresenterWidget.MyView> implements GWASUploadWizardUiHandlers {


    private boolean multipleUpload = true;
    private String restURL = "provider/gwas/upload";
    private final Analytics analytics;

    public boolean isMultipleUpload() {
        return multipleUpload;
    }

    public interface MyView extends View, HasUiHandlers<GWASUploadWizardUiHandlers> {

        void setmultipleUpload(boolean multipleUpload);

        void setRestURL(String restUrl);
    }

    @Inject
    public GWASUploadWizardPresenterWidget(EventBus eventBus, MyView view, final Analytics analytics) {
        super(eventBus, view);
        this.analytics = analytics;
        getView().setUiHandlers(this);
    }

    @Override
    public void onClose() {
        GWASUploadedEvent.fire(getEventBus(), 0L);
    }

    @Override
    public void onUploadStart() {
        analytics.startTimingEvent("GWAS", "Upload");
        fireEvent(new LoadingIndicatorEvent(true, "Uploading..."));
    }

    @Override
    public void onUploadEnd() {
        fireEvent(new LoadingIndicatorEvent(false));
        analytics.endTimingEvent("GWAS", "Upload").userTimingLabel("SUCESS").go();
        GoogleAnalyticsEvent.fire(getEventBus(), new GoogleAnalyticsEvent.GAEventData("GWAS", "Upload", "URL:" + this.restURL));
    }

    @Override
    public void onUploadError(String errorMessage) {
        fireEvent(new LoadingIndicatorEvent(false));
        analytics.endTimingEvent("GWAS", "Upload").userTimingLabel("ERROR").go();
        GoogleAnalyticsEvent.fire(getEventBus(), new GoogleAnalyticsEvent.GAEventData("GWAS", "Error - Upload", "URL:" + this.restURL + ",Error:" + errorMessage));
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
