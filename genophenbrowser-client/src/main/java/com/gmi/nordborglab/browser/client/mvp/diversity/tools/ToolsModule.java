package com.gmi.nordborglab.browser.client.mvp.diversity.tools;

import com.gmi.nordborglab.browser.client.mvp.diversity.tools.gwasviewer.GWASViewerModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class ToolsModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new GWASViewerModule());
    }
}
