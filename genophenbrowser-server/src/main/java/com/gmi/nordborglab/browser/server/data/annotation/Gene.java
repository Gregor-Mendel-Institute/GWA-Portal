package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */

@JsonSerialize(using = GeneSerializer.class)
public class Gene {

    private final long start;
    private final long end;
    private final int strand;
    private final String name;
    private final List<GeneFeature> features;

    public Gene(long start, long end, int strand,String name, List<GeneFeature> features) {
        this.start = start;
        this.end = end;
        this.name = name;
        this.strand = strand;
        this.features = features;
    }


    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getStrand() {
        return strand;
    }

    public String getName() {
        return name;
    }

    public List<GeneFeature> getFeatures() {
        return features;
    }
}
