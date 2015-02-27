package com.gmi.nordborglab.browser.server.data.annotation;

import java.util.List;

/**
 * Created by uemit.seren on 10/22/14.
 */
public class SNPAlleleInfo {

    protected SNPInfo snpInfo;
    protected List<Byte> alleles;

    public SNPAlleleInfo(SNPInfo annot, List<Byte> alleles) {
        this.snpInfo = annot;
        this.alleles = alleles;
    }

    public SNPInfo getSnpInfo() {
        return snpInfo;
    }

    public void setSnpInfo(SNPInfo snpInfo) {
        this.snpInfo = snpInfo;
    }

    public List<Byte> getAlleles() {
        return alleles;
    }

    public void setAlleles(List<Byte> alleles) {
        this.alleles = alleles;
    }
}

