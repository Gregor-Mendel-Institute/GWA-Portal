package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import java.util.List;

/**
 * Created by uemit.seren on 1/15/15.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.rest.SampleData")
public interface SampleDataProxy extends ValueProxy {

    String getSourceId();

    Long getStockId();

    Long getPassportId();

    Double getLongitude();

    Double getLatitude();

    String getCountry();

    String getCountryShort();

    String getAccessionName();

    @AssertFalse
    boolean isParseError();

    @AssertTrue
    boolean isIdKnown();

    public List<String> getValues();

    int getParseMask();
}
