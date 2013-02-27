package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */


@ProxyForName(value="com.gmi.nordborglab.browser.server.domain.phenotype.TransformationData")
public interface TransformationDataProxy extends ValueProxy{

    Double getShapiroPval();

    public enum TYPE {RAW,LOG,SQRT,BOXCOX}

    public TYPE getType();
    public List<Double> getValues();

}
