package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.PassportSearchCriteria;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.pages.PassportPage;
import com.gmi.nordborglab.browser.server.domain.stats.PassportStats;

import java.util.List;

public interface PassportService {

    public PassportPage findAll(Long taxonomyId, PassportSearchCriteria filter, int start, int size);

    public Passport findOne(Long passportId);

    public PassportStats findStats(Long passportId);

    public List<Stock> findAllStocks(Long passportId);

}
