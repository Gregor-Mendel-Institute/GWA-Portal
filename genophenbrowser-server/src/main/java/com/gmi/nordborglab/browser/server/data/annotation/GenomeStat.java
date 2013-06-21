package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class GenomeStat {

    private String name;
    private String label;
    private boolean isStackable = false;
    private boolean isStepPlot = true;

    public GenomeStat(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public boolean isStepPlot() {
        return isStepPlot;
    }
}
