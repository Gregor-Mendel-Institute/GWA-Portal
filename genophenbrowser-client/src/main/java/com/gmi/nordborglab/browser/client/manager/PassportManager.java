package com.gmi.nordborglab.browser.client.manager;


import java.util.List;

import com.gmi.nordborglab.browser.client.mvp.presenter.germplasm.passport.PassportListPresenter.PassportProxyFilter;
import com.gmi.nordborglab.browser.shared.proxy.PassportPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportSearchCriteriaProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportStatsProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StockProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PassportRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class PassportManager extends RequestFactoryManager<PassportRequest> {

	@Inject
	public PassportManager(CustomRequestFactory rf) {
		super(rf);
	}

	@Override
	public PassportRequest getContext() {
		return rf.passportRequest();
	}
	
	public void findAll(Receiver<PassportPageProxy> receiver,Long taxonomyId,PassportProxyFilter filter,int start, int size) {
		PassportRequest ctx = rf.passportRequest();
		PassportSearchCriteriaProxy passportProxyFilter = ctx.create(PassportSearchCriteriaProxy.class);
		filter.apply(passportProxyFilter);
		ctx.findAll(taxonomyId,passportProxyFilter,start,size).with("content.collection.locality","content.source","content.sampstat","content.alleleAssays").fire(receiver);
	}

	public void findOne(Receiver<PassportProxy> receiver, Long passportId) {
		rf.passportRequest().findOne(passportId).with("collection.locality","source","sampstat","alleleAssays","taxonomy").fire(receiver);
	}
	
	public void findStats(Receiver<PassportStatsProxy> receiver,Long passportId) {
		rf.passportRequest().findStats(passportId).fire(receiver);
	}
	
	public void findAllStocks(Long passportId,	Receiver<List<StockProxy>> receiver) {
		rf.passportRequest().findAllStocks(passportId).with("generation").fire(receiver);
	}

	public void findAllPhenotypes(Long passportId,
			Receiver<List<PhenotypeProxy>> receiver) {
		rf.phenotypeRequest().findPhenotypesByPassportId(passportId).with("statisticTypes","unitOfMeasure").fire(receiver);
		
	}
	
	public void findAllStudies(Long passportId,Receiver<List<StudyProxy>> receiver) {
		rf.cdvRequest().findStudiesByPassportId(passportId).with("alleleAssay","protocol").fire(receiver);
	}
	
}

