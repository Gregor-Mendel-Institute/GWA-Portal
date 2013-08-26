package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 10:19
 * To change this template use File | Settings | File Templates.
 */
@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.stats.AppStat")
public interface AppStatProxy extends ValueProxy {
    public enum STAT {USER, STUDY, PHENOTYPE, ANALYSIS, ONTOLOGY, PUBLICATION, PASSPORT, GENOTYPES, STOCKS}

    public STAT getStat();

    public long getValue();
}
