package com.gmi.nordborglab.browser.server.data.annotation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/4/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */

@JsonSerialize(using = GeneSerializer.class)
public class Gene {

    protected final long start;
    protected final long end;
    protected final int strand;
    protected final String name;
    protected final List<GeneFeature> features;
    protected String symbol;
    protected List<String> synonyms = Lists.newArrayList();
    protected String description;
    protected String shortDescription;
    protected String curatorSummary;
    protected String annotation;
    protected List<GoTerm> goTerms = Lists.newArrayList();

    public Gene(long start, long end, int strand, String name, List<GeneFeature> features) {
        this.start = start;
        this.end = end;
        this.name = name;
        this.strand = strand;
        this.features = features;
    }


    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getStrand() {
        return strand;
    }

    public String getName() {
        return name;
    }

    public List<GeneFeature> getFeatures() {
        return features;
    }

    public String getChr() {
        if (name != null && name.length() >= 3) {
            return name.substring(2, 3);
        }
        return null;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getCuratorSummary() {
        return curatorSummary;
    }

    public void setCuratorSummary(String curatorSummary) {
        this.curatorSummary = curatorSummary;
    }

    public List<GoTerm> getGoTerms() {
        return goTerms;
    }

    public void setGoTerms(List<GoTerm> goTerms) {
        this.goTerms = goTerms;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene)) return false;

        final Gene gene = (Gene) o;

        return Objects.equals(this.name, gene.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
