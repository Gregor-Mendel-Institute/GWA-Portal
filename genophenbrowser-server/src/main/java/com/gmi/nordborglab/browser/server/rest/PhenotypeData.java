package com.gmi.nordborglab.browser.server.rest;

import java.util.List;

/**
 * Created by uemit.seren on 14.01.14.
 */
public class PhenotypeData {

    private String transformation;
    private String filename;
    private List<PhenotypeValue> values;

    public PhenotypeData() {
    }

    public PhenotypeData(String transformation, String filename, List<PhenotypeValue> values) {
        this.transformation = transformation;
        this.filename = filename;
        this.values = values;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getFilename() {
        return filename;
    }

    public List<PhenotypeValue> getValues() {
        return values;
    }
}
