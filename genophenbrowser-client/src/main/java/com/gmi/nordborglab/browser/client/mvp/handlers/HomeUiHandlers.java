package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramFacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */
public interface HomeUiHandlers extends UiHandlers {

    public void onChangeChartType(DateStatHistogramFacetProxy.TYPE type);

    void onChangeChartInterval(DateStatHistogramProxy.INTERVAL interval);
}
