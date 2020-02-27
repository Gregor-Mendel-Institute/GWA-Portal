package com.gmi.nordborglab.browser.server.domain.stats;

import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramFacetProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
public class DateStatHistogramFacet {

    private List<DateStatHistogram> histogram;
    private DateStatHistogramFacetProxy.TYPE type;

    public DateStatHistogramFacet() {
    }

    public DateStatHistogramFacet(List<DateStatHistogram> histogram, DateStatHistogramFacetProxy.TYPE type) {
        this.histogram = histogram;
        this.type = type;
    }

    public List<DateStatHistogram> getHistogram() {
        return histogram;
    }

    public DateStatHistogramFacetProxy.TYPE getType() {
        return type;
    }
}
