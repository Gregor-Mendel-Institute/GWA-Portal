package com.gmi.nordborglab.browser.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

/**
 * Created by uemit.seren on 12/18/14.
 */

@ProxyForName(value = "com.gmi.nordborglab.browser.server.domain.util.GWASRuntimeInfo", locator = "com.gmi.nordborglab.browser.server.service.SpringEntitiyLocator")
public interface GWASRuntimeInfoProxy extends EntityProxy {

    public AlleleAssayProxy getAlleleAssay();

    public StudyProtocolProxy getStudyProtocol();

    public Long getAlleleAssayId();

    public Long getStudyProtocolId();

    public Double getCoefficient1();

    public Double getCoefficient2();

    public Double getCoefficient3();

}
