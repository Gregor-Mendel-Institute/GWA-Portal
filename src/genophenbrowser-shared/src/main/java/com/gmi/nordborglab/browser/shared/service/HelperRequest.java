package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramFacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.UserNotificationProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;


@ServiceName(value = "com.gmi.nordborglab.browser.server.service.HelperService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface HelperRequest extends RequestContext {

    Request<List<BreadcrumbItemProxy>> getBreadcrumbs(Long id, String object);

    Request<AppDataProxy> getAppData();

    Request<List<TransformationDataProxy>> calculateTransformations(List<TraitProxy> values, Long alleleAssayId);

    Request<List<UserNotificationProxy>> getUserNotifications(Integer limit);

    Request<List<DateStatHistogramFacetProxy>> findRecentTraitHistogram(DateStatHistogramProxy.INTERVAL interval);

    Request<Double> calculatePseudoHeritability(List<TraitProxy> traits, TransformationDataProxy.TYPE type, Long alleleAssayId);
}
