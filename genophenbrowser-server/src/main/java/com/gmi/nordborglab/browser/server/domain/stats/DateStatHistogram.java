package com.gmi.nordborglab.browser.server.domain.stats;

import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 26.06.13
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class DateStatHistogram {


    private Date time;
    private Long value;
    private DateStatHistogramProxy.INTERVAL interval;

    public DateStatHistogram() {
    }

    public DateStatHistogram(Date time, Long value, DateStatHistogramProxy.INTERVAL interval) {
        this.time = time;
        this.value = value;
        this.interval = interval;
    }

    public Date getTime() {
        return time;
    }

    public Long getValue() {
        return value;
    }

    public DateStatHistogramProxy.INTERVAL getInterval() {
        return interval;
    }
}
