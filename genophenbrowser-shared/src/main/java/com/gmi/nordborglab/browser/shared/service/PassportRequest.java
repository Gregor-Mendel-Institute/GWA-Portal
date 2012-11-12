package com.gmi.nordborglab.browser.shared.service;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.PassportPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportSearchCriteriaProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportStatsProxy;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.PassportService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface PassportRequest extends RequestContext {

	Request<PassportPageProxy> findAll(Long taxonomyId, PassportSearchCriteriaProxy filter,
			int start, int size);

	Request<PassportProxy> findOne(Long passportId);
	
	Request<PassportStatsProxy> findStats(Long passportId);
	
	Request<List<StockProxy>> findAllStocks(Long passportId);

}
