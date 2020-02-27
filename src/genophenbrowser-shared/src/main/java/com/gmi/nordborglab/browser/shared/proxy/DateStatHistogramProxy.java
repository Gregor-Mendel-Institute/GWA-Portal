package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.stats.DateStatHistogram")
public interface DateStatHistogramProxy extends ValueProxy {

    public static enum INTERVAL {YEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE}

    public Date getTime();

    public Long getValue();

    public INTERVAL getInterval();

}
