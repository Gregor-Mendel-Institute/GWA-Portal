package com.gmi.nordborglab.browser.shared.service;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.ObsUnitPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.ObsUnitService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface ObsUnitRequest extends RequestContext {
	Request<ObsUnitPageProxy> findObsUnits(Long id,int start,int size);
	Request<List<ObsUnitProxy>> findObsUnitWithNoGenotype(Long phenotypeId,Long alleleAssayId);
}
