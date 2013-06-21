package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public class GenomeStatsDataResultStatus extends ResultStatus<String> {

    public GenomeStatsDataResultStatus(String status, String statustext, String data) {
        super(status, statustext, data);
    }

    public String getData() {
        return data;
    }
}
