package com.gmi.nordborglab.browser.client.dto;

import com.gmi.nordborglab.browser.client.util.DataTableUtils;
import com.googlecode.gwt.charts.client.DataTable;

import java.util.ArrayList;
import java.util.List;

public class GWASDataDTO {

    protected final double maxScore;
    protected final double bonferroniThreshold;
    protected final List<DataTable> gwasDataTables = new ArrayList<DataTable>();
    protected final List<Integer> chrLengths = new ArrayList<Integer>();
    protected final List<String> chromosomes = new ArrayList<>();
    protected final boolean hasLdData;

    public GWASDataDTO(GWASData data) {
        this.maxScore = data.getMaxScore();
        this.bonferroniThreshold = data.getBonferroniThreshold();
        for (int i = 0; i < data.getChrLengths().length(); i++) {
            chrLengths.add(data.getChrLengths().get(i));
        }

        for (int i = 0; i < data.getChromosomes().length(); i++) {
            chromosomes.add(data.getChromosomes().get(i));
        }

        for (int i = 0; i < data.getGWASDataTablesJSON().length(); i++) {
            gwasDataTables.add(DataTableUtils.createDataTable(data.getGWASDataTablesJSON().get(i)));
        }
        this.hasLdData = data.hasLdData();
    }


    public double getMaxScore() {
        return maxScore;
    }


    public double getBonferroniThreshold() {
        return bonferroniThreshold;
    }


    public List<DataTable> getGwasDataTables() {
        return gwasDataTables;
    }

    public List<String> getChromosomes() {
        return chromosomes;
    }

    public List<Integer> getChrLengths() {
        return chrLengths;
    }

    public boolean hasLdData() {
        return hasLdData;
    }
}
