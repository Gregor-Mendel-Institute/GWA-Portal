package com.gmi.nordborglab.browser.server.rest;


import java.util.List;

public class PhenotypeUploadValue {

    private String sourceId;
    private Long stockId;
    private Long passportId;
    private String accessionName;
    private List<String> values;
    private boolean hasParseError = false;
    private boolean isIdKnown = false;

    public PhenotypeUploadValue() {
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

    public boolean hasParseError() {
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

    public void setParseError(boolean hasParseError) {
        this.hasParseError = hasParseError;
    }

    public void setIdKnown(boolean idKnown) {
        isIdKnown = idKnown;
    }

}
