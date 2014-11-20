package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created by uemit.seren on 11/19/14.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.SNPGWASInfo")
public interface SNPGWASInfoProxy extends ValueProxy {

    public double getScore();

    public double getBonferroniScore();

    public double getMaf();

    public int getMac();

    public double getGVE();

    public long getNumberOfSNPs();

    public String getChr();

    public int getPosition();

    public double getMaxScore();
}
