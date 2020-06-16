package com.gmi.nordborglab.browser.server.data.annotation;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;

import java.util.List;

/**
 * Created by uemit.seren on 10/22/14.
 */
public class SNPAlleleInfo {

    protected SNPInfo snpInfo;
    protected List<Byte> alleles;
    protected List<Passport> passports;

    public SNPAlleleInfo(SNPInfo annot, List<Byte> alleles, List<Passport> passports) {
        this.snpInfo = annot;
        this.alleles = alleles;
        this.passports = passports;
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

    public List<Passport> getPassports() {
        return passports;
    }

    public void setPassports(List<Passport> passports) {
        this.passports = passports;
    }
}

