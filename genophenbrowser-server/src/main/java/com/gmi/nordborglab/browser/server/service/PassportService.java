package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.PassportSearchCriteria;
import com.gmi.nordborglab.browser.server.domain.PassportStats;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.pages.PassportPage;

public interface PassportService {
	
	public PassportPage findAll(Long taxonomyId,PassportSearchCriteria filter, int start, int size);
	
	public Passport findOne(Long passportId);
	
	public PassportStats findStats(Long passportId);
	
	public List<Stock> findAllStocks(Long passportId);

}
