package com.gmi.nordborglab.browser.server.rest;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadData {

    private boolean hasError=false;
    private String name;
    private String unitOfMeasure;
    private String protocol;
    private String traitOntology;
    private String environmentOntology;

    private List<PhenotypeValue> phenotypeValues;


    public PhenotypeUploadData() {
    }

    public PhenotypeUploadData(boolean hasError, String name, String unitOfMeasure, String protocol, String traitOntology, String environmentOntology, List<PhenotypeValue> phenotypeValues) {
        this.hasError = hasError;
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
        this.protocol = protocol;
        this.traitOntology = traitOntology;
        this.environmentOntology = environmentOntology;
        this.phenotypeValues = phenotypeValues;
    }

    public boolean isHasError() {
        return hasError;
    }

    public List<PhenotypeValue> getPhenotypeValues() {
        return phenotypeValues;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
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
}


