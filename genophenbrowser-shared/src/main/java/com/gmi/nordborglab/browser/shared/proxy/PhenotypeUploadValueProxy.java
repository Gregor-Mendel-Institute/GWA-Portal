package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/12/13
 * Time: 10:44 AM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value="com.gmi.nordborglab.browser.server.rest.PhenotypeUploadValue")
public interface PhenotypeUploadValueProxy extends ValueProxy {

    String getSourceId();

    Long getStockId();

    Long getPassportId();

    String getAccessionName();

    List<String> getValues();

    boolean isParseError();

    boolean isIdKnown();

}
