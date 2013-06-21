package com.gmi.nordborglab.browser.server.data.es;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public class ESTermsFacet {

    protected String term;
    protected double value;

    public ESTermsFacet() {
    }

    public ESTermsFacet(String term, double value) {
        this.term = term;
        this.value = value;
    }

    public String getTerm() {
        return term;
    }

    public double getValue() {
        return value;
    }
}
