package com.gmi.nordborglab.browser.server.data;

/**
 * Created by uemit.seren on 11/18/14.
 */
public class SNPGWASInfo {

    private final double score;
    private final double bonferroniScore;
    private final double maf;
    private final int mac;
    private final double GVE;
    private final long numberOfSNPs;
    private final String chr;
    private final int position;
    private final double maxScore;


    public SNPGWASInfo(String chr, int position, double score, int numberOfSNPs, int mac, double maf, double GVE, double bonferroniScore, double maxScore) {
        this.chr = chr;
        this.position = position;
        this.score = score;
        this.numberOfSNPs = numberOfSNPs;
        this.mac = mac;
        this.maf = maf;
        this.bonferroniScore = bonferroniScore;
        this.GVE = GVE;
        this.maxScore = maxScore;

    }

    public double getScore() {
        return score;
    }

    public double getBonferroniScore() {
        return bonferroniScore;
    }

    public double getMaf() {
        return maf;
    }

    public int getMac() {
        return mac;
    }

    public double getGVE() {
        return GVE;
    }

    public long getNumberOfSNPs() {
        return numberOfSNPs;
    }

    public String getChr() {
        return chr;
    }

    public int getPosition() {
        return position;
    }

    public double getMaxScore() {
        return maxScore;
    }
}
