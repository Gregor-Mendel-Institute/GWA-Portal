package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/2/13
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SNPAnnot {

    protected long position;
    protected String chr;
    protected Boolean inGene;
    protected String alt;
    protected String ref;
    protected String lyr;
    protected String annotation;

    public SNPAnnot() {
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

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
