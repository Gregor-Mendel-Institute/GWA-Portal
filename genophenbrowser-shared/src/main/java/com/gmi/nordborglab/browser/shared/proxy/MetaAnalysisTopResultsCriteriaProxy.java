package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 11.06.13
 * Time: 19:43
 * To change this template use File | Settings | File Templates.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria")
public interface MetaAnalysisTopResultsCriteriaProxy extends ValueProxy {
    public String getChr();

    public void setChr(String chr);

    public Boolean isInGene();

    public void setInGene(Boolean inGene);

    public Boolean isOverFDR();

    public void setOverFDR(Boolean overFDR);

    public String getAnnotation();

    public void setAnnotation(String annotation);
}
