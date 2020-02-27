package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
public abstract class ResultStatus<T> {

    protected String status;
    protected String statustext;
    protected T data;

    public ResultStatus(String status, String statustext, T data) {
        this.status = status;
        this.statustext = statustext;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public String getStatustext() {
        return statustext;
    }
}
