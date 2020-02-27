package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/6/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class FetchGeneInfoResult {

    private final String status = "OK";
    private final String description;

    public FetchGeneInfoResult(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
