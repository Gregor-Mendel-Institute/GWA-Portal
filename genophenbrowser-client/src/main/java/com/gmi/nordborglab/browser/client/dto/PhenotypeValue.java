package com.gmi.nordborglab.browser.client.dto;

import com.google.gwt.query.client.js.JsMap;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/12/13
 * Time: 10:44 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PhenotypeValue {
    String getSourceId();

    Long getStockId();

    Long getPassportId();

    String getAccessionName();

    List<String> getValues();

    boolean hasParseError();

    boolean isIdKnown();
}
