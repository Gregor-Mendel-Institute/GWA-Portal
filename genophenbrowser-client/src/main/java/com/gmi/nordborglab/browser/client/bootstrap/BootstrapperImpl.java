package com.gmi.nordborglab.browser.client.bootstrap;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.util.ParallelRunnable;
import com.gmi.nordborglab.browser.client.util.ParentCallback;
import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.MotionChart;
import com.google.gwt.visualization.client.visualizations.OrgChart;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.Bootstrapper;
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

    @Inject
    public BootstrapperImpl(PlaceManager placeManager, CurrentUser currentUser,
                            CustomRequestFactory rf, AppUserFactory appUserFactory
    ) {
        this.placeManager = placeManager;
        this.currentUser = currentUser;
        this.rf = rf;
        this.appUserFactory = appUserFactory;
    }

    @Override
    public void onBootstrap() {
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

        // Can't be used because of   https://code.google.com/p/gwt-charts/issues/detail?id=40
        //ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
        //chartLoader.loadApi(chartsRunnable);

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
