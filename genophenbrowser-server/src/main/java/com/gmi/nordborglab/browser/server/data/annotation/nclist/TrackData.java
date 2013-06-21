package com.gmi.nordborglab.browser.server.data.annotation.nclist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackData {

    private List<NCListItem> featureNCList;
    private String lazyfeatureUrlTemplate;
    private List<HistogramMeta> histogramMeta;

    public TrackData() {
    }

    public List<NCListItem> getFeatureNCList() {
        return featureNCList;
    }

    public String getLazyfeatureUrlTemplate() {
        return lazyfeatureUrlTemplate;
    }

    public List<HistogramMeta> getHistogramMeta() {
        return histogramMeta;
    }
}
