package com.gmi.nordborglab.browser.client.dto;

import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;

/**
 * Created by uemit.seren on 10/29/14.
 */
public class SNPAllele {

    protected int rowid;
    protected PassportProxy passport;
    protected String allele;
    protected String phenotype;

    public SNPAllele(int rowid, PassportProxy passport, String allele, String phenotype) {
        this.rowid = rowid;
        this.passport = passport;
        this.allele = allele;
        this.phenotype = phenotype;
    }

    public int getRowid() {
        return rowid;
    }

    public PassportProxy getPassport() {
        return passport;
    }

    public String getAllele() {
        return allele;
    }

    public String getPhenotype() {
        return phenotype;
    }
}
