package com.gmi.nordborglab.browser.server.data.isatab;

import java.util.List;
import java.util.Map;

/**
 * Created by uemit.seren on 6/4/14.
 */
public class IsaTabDerivedData {

    private final List<String> phenotypes;
    private final List<String> assays;
    private final List<List<String>> data;

    public IsaTabDerivedData(List<String> phenotypes, List<String> assays, List<List<String>> data) {
        this.phenotypes = phenotypes;
        this.assays = assays;
        this.data = data;
    }

    public List<String> getPhenotypes() {
        return phenotypes;
    }

    public List<String> getAssays() {
        return assays;
    }

    public List<List<String>> getData() {
        return data;
    }
}
