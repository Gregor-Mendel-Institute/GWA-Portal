package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/6/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Isoform {

    protected String name;

    protected String RNA_type;
    protected Long start_pos;
    protected Long end_pos;
    protected String type;
    protected Character strand;
    protected String computational_description;
    protected String short_description;
    protected String curator_summary;

    public Isoform() {
    }


    public String getName() {
        return name;
    }

    public String getRNA_type() {
        return RNA_type;
    }

    public Long getStart_pos() {
        return start_pos;
    }

    public Long getEnd_pos() {
        return end_pos;
    }

    public String getType() {
        return type;
    }

    public Character getStrand() {
        return strand;
    }

    public String getComputational_description() {
        return computational_description;
    }

    public String getShort_description() {
        return short_description;
    }

    public String getCurator_summary() {
        return curator_summary;
    }
}
