package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.StockRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class StockManager extends RequestFactoryManager<StockRequest> {
	
	@Inject
	public StockManager(CustomRequestFactory rf) {
		super(rf);
	}

	@Override
	public StockRequest getContext() {
		return rf.stockRequest();
	}
	
	public void findOne(Receiver<StockProxy> receiver,Long stockId) {
		getContext().findOne(stockId).with("generation","passport").fire(receiver);
	}


}
