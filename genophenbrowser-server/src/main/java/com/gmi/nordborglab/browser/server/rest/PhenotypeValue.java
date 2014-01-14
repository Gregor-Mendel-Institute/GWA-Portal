package com.gmi.nordborglab.browser.server.rest;

/**
 * Created by uemit.seren on 14.01.14.
 */
public class PhenotypeValue {

    private Long passportId;
    private String value;

    public PhenotypeValue() {
    }

    public PhenotypeValue(Long passportId, String value) {
        this.passportId = passportId;
        this.value = value;
    }

    public Long getPassportId() {
        return passportId;
    }

    public String getValue() {
        return value;
    }
}
