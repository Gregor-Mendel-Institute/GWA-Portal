package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.TraitService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface TraitRequest extends RequestContext {

    Request<List<TraitProxy>> findAllTraitValues(Long phenotypeId, Long alleleAssayId, Long statisticTypeId);

    Request<List<TraitProxy>> findAllTraitValuesByStatisticType(Long phenotypeId, Long statisticTypeId);
}
