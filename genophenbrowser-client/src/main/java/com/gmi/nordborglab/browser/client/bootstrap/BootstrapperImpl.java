package com.gmi.nordborglab.browser.client.bootstrap;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;
import com.eemi.gwt.tour.client.GwtTour;
import com.gmi.nordborglab.browser.client.events.GoogleAnalyticsEvent;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.ParallelRunnable;
import com.gmi.nordborglab.browser.client.util.ParentCallback;
import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.OrgChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.googleanalytics.GoogleAnalytics;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by uemit.seren on 11.01.14.
 */
public class BootstrapperImpl implements Bootstrapper {
    private final PlaceManager placeManager;
    private final CurrentUser currentUser;
    private final CustomRequestFactory rf;
    private final AppUserFactory appUserFactory;
    private final GoogleAnalytics googleAnalytics;
    private final EventBus eventBus;

    @Inject
    public BootstrapperImpl(PlaceManager placeManager, CurrentUser currentUser,
                            CustomRequestFactory rf, AppUserFactory appUserFactory,
                            GoogleAnalytics googleAnalytics, EventBus eventBus

    ) {
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        this.rf = rf;
        this.eventBus = eventBus;
        this.appUserFactory = appUserFactory;
        this.googleAnalytics = googleAnalytics;
    }

    @Override
    public void onBootstrap() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            @Override
            public void onUncaughtException(Throwable e) {
                Logger logger = Logger.getLogger("uncaught");
                logger.log(Level.SEVERE, "Uncaught Exception" + e.getMessage(), e);
                int userId = currentUser.getUserId();
                String place = "";
                if (placeManager != null) {
                    place = placeManager.buildHistoryToken(placeManager.getCurrentPlaceRequest());
                }
                googleAnalytics.trackEvent("Errors", "Uncaught", "Place: " + place + ", User:" + userId + ", Exception:" + e.getMessage(), 0, true);
            }
        });

        eventBus.addHandler(GoogleAnalyticsEvent.TYPE, new GoogleAnalyticsEvent.Handler() {
            @Override
            public void onTrack(GoogleAnalyticsEvent event) {
                int userId = currentUser.getUserId();
                GoogleAnalyticsEvent.GAEventData data = event.getEventData();
                // required because otehrwiase intValue will cause nullpointer
                if (data.getValue() == null) {
                    googleAnalytics.trackEvent(data.getCategory(), data.getAction(), data.getLabel() + ", UserId:" + userId);
                } else {
                    googleAnalytics.trackEvent(data.getCategory(), data.getAction(), data.getLabel() + ", UserId:" + userId, data.getValue(), data.isNoninteraction());
                }
            }
        });

        GwtTour.load();
        initUserData();
        final ParallelRunnable visualizationRunnable = new ParallelRunnable();
        final ParallelRunnable rfRunnalbe = new ParallelRunnable();
        final ParallelRunnable mapsRunnable = new ParallelRunnable();
        final ParallelRunnable chartsRunnable = new ParallelRunnable();
        Receiver<AppDataProxy> receiver = new Receiver<AppDataProxy>() {

            @Override
            public void onSuccess(AppDataProxy response) {
                currentUser.setAppData(response);
                rfRunnalbe.run();
            }
        };

        ParentCallback parentCallback = new ParentCallback(visualizationRunnable, rfRunnalbe, mapsRunnable) {

            @Override
            protected void handleSuccess() {
                placeManager.revealCurrentPlace();
            }
        };
        /* FIXME https://code.google.com/p/gwt-charts/issues/detail?id=53 */
        /*ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART,ChartPackage.ORGCHART,ChartPackage.GEOCHART);
        chartLoader.loadApi(chartsRunnable);*/

        VisualizationUtils.loadVisualizationApi(visualizationRunnable, CoreChart.PACKAGE, MotionChart.PACKAGE, GeoChart.PACKAGE, OrgChart.PACKAGE);

        // load all the libs for use in the maps
        ArrayList<LoadApi.LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
        loadLibraries.add(LoadApi.LoadLibrary.ADSENSE);
        loadLibraries.add(LoadApi.LoadLibrary.DRAWING);
        loadLibraries.add(LoadApi.LoadLibrary.GEOMETRY);
        loadLibraries.add(LoadApi.LoadLibrary.PANORAMIO);
        loadLibraries.add(LoadApi.LoadLibrary.PLACES);
        loadLibraries.add(LoadApi.LoadLibrary.WEATHER);
        LoadApi.go(mapsRunnable, loadLibraries, false);


        if (currentUser.getAppData() == null) {
            rf.helperRequest().getAppData().with("news.author").fire(receiver);
        } else {
            rfRunnalbe.run();
        }

    }

    private void initUserData() {
        String userData = getUserData();
        if (userData != null) {
            try {
                AutoBean<AppUserProxy> userBean = AutoBeanCodex.decode(appUserFactory, AppUserProxy.class, userData);
                currentUser.setAppUser(userBean.as());
            } catch (Exception e) {
                Logger logger = Logger.getLogger("");
                logger.log(Level.SEVERE, "Autobean decoding", e);
            }
        }
    }

    private String getAppData() {
        String appData = null;
        try {
            Dictionary data = Dictionary.getDictionary("appData");
            if (data != null) {
                appData = data.get("data");
            }
        } catch (Exception e) {
        }
        return appData;
    }

    protected String getUserData() {
        String user = null;
        try {
            Dictionary data = Dictionary.getDictionary("userData");
            if (data != null) {
                user = data.get("user");
            }
        } catch (Exception e) {
        }
        return user;
    }
}
