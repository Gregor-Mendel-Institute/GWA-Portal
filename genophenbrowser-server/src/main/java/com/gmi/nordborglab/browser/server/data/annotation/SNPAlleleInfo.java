package com.gmi.nordborglab.browser.server.data.annotation;

import java.util.List;

/**
 * Created by uemit.seren on 10/22/14.
 */
public class SNPAlleleInfo {

    protected SNPAnnot snpAnnot;
    protected List<Byte> alleles;

    public SNPAlleleInfo(SNPAnnot annot, List<Byte> alleles) {
        this.snpAnnot = annot;
        this.alleles = alleles;
    }

    public SNPAnnot getSnpAnnot() {
        return snpAnnot;
    }

    public void setSnpAnnot(SNPAnnot snpAnnot) {
        this.snpAnnot = snpAnnot;
    }

    public List<Byte> getAlleles() {
        return alleles;
    }

    public void setAlleles(List<Byte> alleles) {
        this.alleles = alleles;
    }
}

