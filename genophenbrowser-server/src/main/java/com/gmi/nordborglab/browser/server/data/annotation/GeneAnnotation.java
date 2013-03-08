package com.gmi.nordborglab.browser.server.data.annotation;


import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/6/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */


public class GeneAnnotation {
    protected String gene_type;
    protected String name;
    protected String chromosome;
    protected Long end_pos;
    protected Long start_pos;
    protected Character strand;
    protected Map<String,Isoform> isoforms;

    public GeneAnnotation() {
    }


    public String getGene_type() {
        return gene_type;
    }

    public String getName() {
        return name;
    }

    public String getChromosome() {
        return chromosome;
    }

    public Long getEnd_pos() {
        return end_pos;
    }

    public Long getStart_pos() {
        return start_pos;
    }

    public Character getStrand() {
        return strand;
    }

    public Map<String, Isoform> getIsoforms() {
        return isoforms;
    }
}
