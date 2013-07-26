package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.05.13
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis")
public interface MetaSNPAnalysisProxy extends ValueProxy {

    public SNPAnnotProxy getSnpAnnotation();

    public Double getPValue();

    public String getAnalysis();

    public String getPhenotype();

    public String getStudy();

    public String getMethod();

    public String getGenotype();

    public Boolean isOverFDR();

    public Long getAnalysisId();

    public Long getPhenotypeId();

    public Long getStudyId();

    public Double getMaf();

    public Integer getMac();
}
