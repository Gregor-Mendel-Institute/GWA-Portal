package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;


public interface StockService {

	public Stock findOne(Long stockId);
}
