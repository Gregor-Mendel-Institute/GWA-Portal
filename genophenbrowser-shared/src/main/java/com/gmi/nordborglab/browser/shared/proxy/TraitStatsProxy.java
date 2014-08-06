package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created by uemit.seren on 8/4/14.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.phenotype.TraitStats")
public interface TraitStatsProxy extends ValueProxy {

    public Long getPassportId();

    public String getAccename();

    public String getOrigcty();

    public String getCountry();

    public Double getLatitude();

    public Double getLongitude();

    public Double getAvgValue();

}
