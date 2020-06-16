package com.gmi.nordborglab.browser.server.data;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/11/13
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASData {

    private final Map<String, ChrGWAData> chrGWASData;
    private final long numberOfSNPs;
    private final double bonferroniScore;
    private final float maxScore;
    private String filename;
    private boolean hasLdData = false;

    public GWASData(Map<String, ChrGWAData> chrGWASData, long numberOfSNPs, double bonferroniScore, float maxScore) {
        this.chrGWASData = chrGWASData;
        this.numberOfSNPs = numberOfSNPs;
        this.bonferroniScore = bonferroniScore;
        this.maxScore = maxScore;
    }

    public Map<String, ChrGWAData> getChrGWASData() {
        return chrGWASData;
    }

    public long getNumberOfSNPs() {
        return numberOfSNPs;
    }

    public double getBonferroniScore() {
        return bonferroniScore;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public void sortByPosition() {
        for (Map.Entry<String, ChrGWAData> dataEntry : chrGWASData.entrySet()) {
            chrGWASData.put(dataEntry.getKey(), ChrGWAData.sortByPosition(dataEntry.getValue()));
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean hasLdData() {
        return hasLdData;
    }

    public void setHasLdData(boolean hasLdData) {
        this.hasLdData = hasLdData;
    }
}
