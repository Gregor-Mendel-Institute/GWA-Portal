package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/12/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value="com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData")
public interface PhenotypeUploadDataProxy extends ValueProxy {
    String getName();

    String getProtocol();

    String getTraitOntology();

    String getEnvironmentOntology();

    String getUnitOfMeasure();

    List<PhenotypeUploadValueProxy> getPhenotypeUploadValues();

    void setPhenotypeUploadValues(List<PhenotypeUploadValueProxy> phenotypeUploadeValues);

    List<String> getValueHeader();

    String getErrorMessage();

    int getErrorValueCount();

}
