package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class GenomeStat {

    private String id;
    private String name;
    private boolean isStackable = false;
    private boolean isStepPlot = true;

    public GenomeStat(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public boolean isStepPlot() {
        return isStepPlot;
    }
}
