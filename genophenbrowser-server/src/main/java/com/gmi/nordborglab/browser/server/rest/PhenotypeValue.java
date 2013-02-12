package com.gmi.nordborglab.browser.server.rest;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/7/13
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PhenotypeValue {

    private String sourceId;
    private Long stockId;
    private Long passportId;
    private String accessionName;
    private List<String> values;
    private boolean hasParseError = false;
    private boolean isIdKnown = false;

    public PhenotypeValue() {
    }


    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public boolean isIdKnown() {
        return isIdKnown;
    }

    public Long getStockId() {
        return stockId;
    }

    public Long getPassportId() {
        return passportId;
    }

    public String getAccessionName() {
        return accessionName;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isHasParseError() {
        return hasParseError;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public void setPassportId(Long passportId) {
        this.passportId = passportId;
    }

    public void setAccessionName(String accessionName) {
        this.accessionName = accessionName;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void setHasParseError(boolean hasParseError) {
        this.hasParseError = hasParseError;
    }

    public void setIdKnown(boolean idKnown) {
        isIdKnown = idKnown;
    }
}
