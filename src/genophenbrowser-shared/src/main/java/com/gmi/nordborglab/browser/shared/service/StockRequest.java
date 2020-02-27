package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.StockService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface StockRequest extends RequestContext {

	Request<StockProxy> findOne(Long stockId);

}
