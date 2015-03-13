package com.gmi.nordborglab.browser.server.data.annotation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/2/13
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SNPInfo {

    protected long position;
    protected String chr;
    protected Boolean inGene;
    protected String alt = "1";
    protected String ref = "0";
    protected String lyr;
    protected List<SNPAnnotation> annotations;
    protected String gene;
    protected Integer refCount;
    protected Integer altCount;


    public SNPInfo() {
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public Boolean isInGene() {
        return inGene;
    }

    public void setInGene(Boolean inGene) {
        this.inGene = inGene;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getLyr() {
        return lyr;
    }

    public void setLyr(String lyr) {
        this.lyr = lyr;
    }

    public List<SNPAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<SNPAnnotation> annotations) {
        this.annotations = annotations;
    }


    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public Integer getRefCount() {
        return refCount;
    }

    public void setRefCount(Integer refCount) {
        this.refCount = refCount;
    }

    public Integer getAltCount() {
        return altCount;
    }

    public void setAltCount(Integer altCount) {
        this.altCount = altCount;
    }
}
