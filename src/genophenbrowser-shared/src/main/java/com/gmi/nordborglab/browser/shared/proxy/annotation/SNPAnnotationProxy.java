package com.gmi.nordborglab.browser.shared.proxy.annotation;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created by uemit.seren on 2/25/15.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.data.annotation.SNPAnnotation")
public interface SNPAnnotationProxy extends ValueProxy {

    String getEffect();

    String getImpact();

    String getFunction();

    String getCodonChange();

    String getAminoAcidChange();

    String getGene();

    String getTrascript();

    Integer getRank();
}
