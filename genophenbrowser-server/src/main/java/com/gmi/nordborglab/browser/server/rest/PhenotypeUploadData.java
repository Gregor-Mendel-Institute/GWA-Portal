package com.gmi.nordborglab.browser.server.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadData {

    private String name;
    private String unitOfMeasure;
    private String protocol;
    private String traitOntology;
    private String environmentOntology;
    private List<PhenotypeValue> phenotypeValues = new ArrayList<PhenotypeValue>();
    private String errorMessage;
    private List<String> valueHeader;
    private int errorValueCount = 0;


    public PhenotypeUploadData() {
    }


    public List<PhenotypeValue> getPhenotypeValues() {
        return phenotypeValues;
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

    public void setPhenotypeValues(List<PhenotypeValue> phenotypeValues) {
        this.phenotypeValues = phenotypeValues;
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

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setValueHeader(List<String> valueHeader) {
        this.valueHeader = valueHeader;
    }

    public List<String> getValueHeader() {
        return valueHeader;
    }

    public void addPhenotypeValue(PhenotypeValue phenotypeValue) {
        if (phenotypeValue.isHasParseError() || phenotypeValue.isIdKnown())
            errorValueCount++;
        phenotypeValues.add(phenotypeValue);
    }

    public int getErrorValueCount() {
        return errorValueCount;
    }
}


