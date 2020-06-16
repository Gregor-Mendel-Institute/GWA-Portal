package com.gmi.nordborglab.browser.server.domain.stats;

import com.gmi.nordborglab.browser.shared.proxy.AppStatProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
public class AppStat {

    private AppStatProxy.STAT stat;
    private long value;

    public AppStat() {

    }

    public AppStat(AppStatProxy.STAT stat, long value) {
        this.stat = stat;
        this.value = value;
    }

    public AppStatProxy.STAT getStat() {
        return stat;
    }

    public long getValue() {
        return value;
    }
}
