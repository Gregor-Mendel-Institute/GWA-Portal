package com.gmi.nordborglab.browser.server.domain.meta;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 19:46
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisTopResultsCriteria {

    protected String chr;
    protected Boolean overFDR;
    protected Boolean inGene;
    protected String annotation;


    public MetaAnalysisTopResultsCriteria() {
    }

    public String getChr() {
        return chr;
    }

    public Boolean isOverFDR() {
        return overFDR;
    }

    public Boolean isInGene() {
        return inGene;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public void setOverFDR(Boolean overFDR) {
        this.overFDR = overFDR;
    }

    public void setInGene(Boolean inGene) {
        this.inGene = inGene;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public boolean isEmpty() {
        return annotation == null && inGene == null && overFDR == null && chr == null;
    }
}
