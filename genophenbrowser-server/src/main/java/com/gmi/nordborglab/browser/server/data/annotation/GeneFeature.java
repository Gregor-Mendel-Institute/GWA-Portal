package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
@JsonDeserialize(using = GeneFeatureDeserializer.class)
@JsonSerialize(using = GeneFeatureSerializer.class)
public class GeneFeature {

    private final long start;
    private final long end;
    private final int strand;
    private final String name;

    public GeneFeature(long start, long end, int strand, String name) {
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.name = name;
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
}
