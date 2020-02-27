package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/12/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData")
public interface PhenotypeUploadDataProxy extends ValueProxy {

    String getName();

    String getProtocol();

    String getTraitOntology();

    String getEnvironmentOntology();

    String getUnitOfMeasure();

    int getErrorCount();

    int getValueCount();

    public int getParseMask();

    void setTraitUom(PhenotypeProxy phenotype);

    @NotNull
    @Valid
    PhenotypeProxy getTraitUom();

    void setConstraintViolation(boolean violation);

    boolean getConstraintViolation();

}
