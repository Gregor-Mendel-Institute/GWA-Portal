package com.gmi.nordborglab.browser.server.rest;

import java.util.List;
import java.util.Map;

/**
 * Created by uemit.seren on 14.01.14.
 */
public class PhenotypeValue {

    private Long passportId;
    private Map<String, String> values;

    public PhenotypeValue() {
    }

    public PhenotypeValue(Long passportId, Map<String, String> values) {
        this.passportId = passportId;
        this.values = values;
    }

    public Long getPassportId() {
        return passportId;
    }

    public Map<String, String> getValue() {
        return values;
    }
}
