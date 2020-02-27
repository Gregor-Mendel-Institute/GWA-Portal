package com.gmi.nordborglab.browser.server.data.annotation;

/**
 * Created by uemit.seren on 2/25/15.
 */
public class SNPAnnotation {

    private String effect;
    private String impact;
    private String function;
    private String codonChange;
    private String aminoAcidChange;
    private String gene;
    private String trascript;
    private Integer rank;

    public SNPAnnotation(String effect) {
        this.effect = effect;
    }

    public SNPAnnotation(String effect, String impact, String function, String codonChange, String aminoAcidChange, String gene, String trascript, Integer rank) {
        this.effect = effect;
        this.impact = impact;
        this.function = function;
        this.codonChange = codonChange;
        this.aminoAcidChange = aminoAcidChange;
        this.gene = gene;
        this.trascript = trascript;
        this.rank = rank;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getCodonChange() {
        return codonChange;
    }

    public void setCodonChange(String codonChange) {
        this.codonChange = codonChange;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getTrascript() {
        return trascript;
    }

    public void setTrascript(String trascript) {
        this.trascript = trascript;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SNPAnnotation that = (SNPAnnotation) o;

        if (aminoAcidChange != null ? !aminoAcidChange.equals(that.aminoAcidChange) : that.aminoAcidChange != null)
            return false;
        if (codonChange != null ? !codonChange.equals(that.codonChange) : that.codonChange != null) return false;
        if (!effect.equals(that.effect)) return false;
        if (function != null ? !function.equals(that.function) : that.function != null) return false;
        if (gene != null ? !gene.equals(that.gene) : that.gene != null) return false;
        if (impact != null ? !impact.equals(that.impact) : that.impact != null) return false;
        if (rank != null ? !rank.equals(that.rank) : that.rank != null) return false;
        if (trascript != null ? !trascript.equals(that.trascript) : that.trascript != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = effect.hashCode();
        result = 31 * result + (impact != null ? impact.hashCode() : 0);
        result = 31 * result + (function != null ? function.hashCode() : 0);
        result = 31 * result + (codonChange != null ? codonChange.hashCode() : 0);
        result = 31 * result + (aminoAcidChange != null ? aminoAcidChange.hashCode() : 0);
        result = 31 * result + (gene != null ? gene.hashCode() : 0);
        result = 31 * result + (trascript != null ? trascript.hashCode() : 0);
        result = 31 * result + (rank != null ? rank.hashCode() : 0);
        return result;
    }
}
