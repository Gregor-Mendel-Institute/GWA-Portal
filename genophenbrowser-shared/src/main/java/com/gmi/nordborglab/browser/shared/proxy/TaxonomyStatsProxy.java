package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.stats.TaxonomyStats")
public interface TaxonomyStatsProxy extends ValueProxy {

    public String getGeoChartData();

    public String getSampStatData();

    public String getAlleleAssayData();

    public String getStockGenerationData();
}
