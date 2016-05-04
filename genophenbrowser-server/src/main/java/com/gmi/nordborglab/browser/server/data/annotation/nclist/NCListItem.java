package com.gmi.nordborglab.browser.server.data.annotation.nclist;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GeneFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/5/13
 * Time: 11:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class NCListItem implements Comparable<NCListItem> {

    private Long start;
    private Long end;
    private int strand;
    private String name;
    private Long chunk;


    private List<GeneFeature> geneFeatures = new ArrayList();
    private List<NCListItem> subNCList;

    public Gene getGene(boolean isFeature) {
        return new Gene(start, end, strand, name, (isFeature ? geneFeatures : new ArrayList<GeneFeature>()));
    }


    public void setEnd(Long end) {
        this.end = end;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChunk(Long chunk) {
        this.chunk = chunk;
    }

    public void setGeneFeatures(List<GeneFeature> geneFeatures) {
        this.geneFeatures = geneFeatures;
    }

    public void setSubNCList(List<NCListItem> subNCList) {
        this.subNCList = subNCList;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public int getStrand() {
        return strand;
    }

    public String getName() {
        return name;
    }

    public Long getChunk() {
        return chunk;
    }

    public List<GeneFeature> getGeneFeatures() {
        return geneFeatures;
    }

    public List<NCListItem> getSubNCList() {
        return subNCList;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    @Override
    public int compareTo(NCListItem o) {
        Long diff = end - o.getStart();
        return diff.intValue();
    }
}
