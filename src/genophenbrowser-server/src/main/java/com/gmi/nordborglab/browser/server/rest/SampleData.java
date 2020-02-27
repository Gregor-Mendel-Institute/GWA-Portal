package com.gmi.nordborglab.browser.server.rest;


import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class SampleData {

    private String sourceId;
    private Long stockId;

    @NotNull
    private Long passportId;
    private Double longitude;
    private Double latitude;
    private String country;
    private String countryShort;

    private String accessionName;

    private boolean isParseError = false;

    private boolean isIdKnown = false;

    @NotNull
    @NotEmpty
    private List<String> values = Lists.newArrayList();

    private int parseMask = 0;

    public SampleData() {

    }

    public SampleData(String sourceId) {
        this.sourceId = sourceId;
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

    public boolean isParseError() {
        return isParseError;
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

    public void setParseError(boolean isParseError) {
        this.isParseError = isParseError;
    }

    public void setIdKnown(boolean idKnown) {
        isIdKnown = idKnown;
    }

    @AssertFalse
    public boolean hasError() {
        return !isIdKnown || isParseError || parseMask > 0;
    }

    public boolean hasIdError() {
        return !isIdKnown || isParseError;
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void addValue(String value, boolean parseError) {
        values.add(value);
        if (parseError)
            parseMask |= (1 << values.size());
    }

    public int getParseMask() {
        return parseMask;
    }


    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryShort() {
        return countryShort;
    }

    public void setCountryShort(String countryShort) {
        this.countryShort = countryShort;
    }

    public boolean isEmptySample() {
        if (this.sourceId == null) {
            return hasNoValues();
        }
        return false;
    }

    public boolean hasNoValues() {
        return values == null || Collections2.filter(values, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String s) {
                return s != null;
            }
        }).size() == 0;
    }

    // to please the RequestFactory
    public void setValues(List<String> values) {
        this.values = values;
    }

    // to please the RequestFactory
    public void setParseMask(int parseMask) {
        this.parseMask = parseMask;
    }
}
