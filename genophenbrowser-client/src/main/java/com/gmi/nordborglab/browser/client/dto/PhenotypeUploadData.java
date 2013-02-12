package com.gmi.nordborglab.browser.client.dto;

import com.google.gwt.core.client.JsArray;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/12/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PhenotypeUploadData {
    String getName();

    String getProtocol();

    String getTraitOntology();

    String getEnvironmentOntology();

    String getUnitOfMeasure();

    List<PhenotypeValue> getPhenotypeValues();

    List<String> getValueHeader();

    String getErrorMessage();

    int getErrorValueCount();

}
