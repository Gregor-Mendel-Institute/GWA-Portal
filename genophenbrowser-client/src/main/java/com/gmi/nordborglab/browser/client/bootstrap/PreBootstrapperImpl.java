package com.gmi.nordborglab.browser.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.PreBootstrapper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by uemit.seren on 11.01.14.
 */
public class PreBootstrapperImpl implements PreBootstrapper {
    @Override
    public void onPreBootstrap() {
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            @Override
            public void onUncaughtException(Throwable e) {
                Logger logger = Logger.getLogger("uncaught");
                logger.log(Level.SEVERE, "Uncaught Exception" + e.getMessage(), e);
            }
        });
    }
}