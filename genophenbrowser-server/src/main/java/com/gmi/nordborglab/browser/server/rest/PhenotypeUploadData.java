package com.gmi.nordborglab.browser.server.rest;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;

import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadData {

    @NotNull
    private TraitUom traitUom;
    private String name;
    private String unitOfMeasure;
    private String protocol;
    private String traitOntology;
    private String environmentOntology;
    private int parseMask = 0;
    private int valueCount = 0;


    public PhenotypeUploadData() {
    }

    public TraitUom getTraitUom() {
        return traitUom;
    }

    public void setTraitUom(TraitUom traitUom) {
        this.traitUom = traitUom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setTraitOntology(String traitOntology) {
        this.traitOntology = traitOntology;
    }

    public void setEnvironmentOntology(String environmentOntology) {
        this.environmentOntology = environmentOntology;
    }

    public String getName() {
        return name;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getTraitOntology() {
        return traitOntology;
    }

    public String getEnvironmentOntology() {
        return environmentOntology;
    }

    public int getParseMask() {
        return parseMask;
    }

    public int getErrorCount() {
        return Integer.bitCount(parseMask);
    }

    public int getValueCount() {
        return valueCount;
    }

    public void incValueCount() {
        this.valueCount += 1;
    }

    public void addParseError(int pos) {
        parseMask = parseMask | (1 << pos);
    }

    public void setParseMask(int parseMask) {
        this.parseMask = parseMask;
    }

    // to please RequestFactory
    public void setValueCount(int valueCount) {
    }

    // to please RequestFactory
    public void setErrorCount(int count) {

    }

    // to please RequestFactory
    public void setConstraintViolation(boolean violation) {

    }

    // to please RequestFactory
    public boolean getConstraintViolation() {
        return false;
    }
}


