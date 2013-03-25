package com.gmi.nordborglab.browser.server.rest;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import org.springframework.util.comparator.BooleanComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeUploadData {

    private TraitUom traitUom;
    private String name;
    private String unitOfMeasure;
    private String protocol;
    private String traitOntology;
    private String environmentOntology;
    private List<PhenotypeUploadValue> phenotypeUploadValues = new ArrayList<PhenotypeUploadValue>();
    private String errorMessage;
    private List<String> valueHeader;
    private int errorValueCount = 0;


    public PhenotypeUploadData() {
    }

    public TraitUom getTraitUom() {
        return traitUom;
    }

    public void setTraitUom(TraitUom traitUom) {
        this.traitUom = traitUom;
    }

    public List<PhenotypeUploadValue> getPhenotypeUploadValues() {
        return phenotypeUploadValues;
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

    public void setPhenotypeUploadValues(List<PhenotypeUploadValue> phenotypeUploadValues) {
        this.phenotypeUploadValues = phenotypeUploadValues;
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

    public void addPhenotypeValue(PhenotypeUploadValue phenotypeUploadValue) {
        if (phenotypeUploadValue.hasParseError() || !phenotypeUploadValue.isIdKnown())
            errorValueCount++;
        phenotypeUploadValues.add(phenotypeUploadValue);
    }

    public int getErrorValueCount() {
        return errorValueCount;
    }

    public void setErrorValueCount(int errorValueCount) {
        this.errorValueCount = errorValueCount;
    }

    public void sortByErrors() {
        Collections.sort(phenotypeUploadValues,new Comparator<PhenotypeUploadValue>() {
            @Override
            public int compare(PhenotypeUploadValue o1, PhenotypeUploadValue o2) {
                return BooleanComparator.TRUE_LOW.compare(o1.hasError(),o2.hasError());
            }
        });
    }
}


