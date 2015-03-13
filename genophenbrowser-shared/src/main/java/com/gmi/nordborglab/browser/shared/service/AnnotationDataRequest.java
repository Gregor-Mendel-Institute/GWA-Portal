package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.SNPAlleleInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/15/13
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.AnnotationDataService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface AnnotationDataRequest extends RequestContext {

    Request<GeneProxy> getGeneById(String id);

    Request<SNPAlleleInfoProxy> getSNPAlleleInfo(Long alelleAssayId, Integer chromosome, Integer position, List<Long> passportIds, boolean fetchPassportInfos);

    Request<SNPInfoPageProxy> getSNPInfosForFilter(Long alleleAssayId, String region, int start, int length, List<Long> passportIds);
}
