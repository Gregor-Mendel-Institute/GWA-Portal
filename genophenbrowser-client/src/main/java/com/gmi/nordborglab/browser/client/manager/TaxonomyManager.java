package com.gmi.nordborglab.browser.client.manager;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.TaxonomyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TaxonomyStatsProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.TaxonomyRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class TaxonomyManager extends RequestFactoryManager<TaxonomyRequest> {

	@Inject
	public TaxonomyManager(CustomRequestFactory rf) {
		super(rf);
	}

	@Override
	public TaxonomyRequest getContext() {
		return rf.taxonomyRequest();
	}
	
	public void findAll(Receiver<List<TaxonomyProxy>> receiver) {
		rf.taxonomyRequest().findAll().with("alleleAssays").fire(receiver);
	}

	public void findOne(Receiver<TaxonomyProxy> receiver, Long taxonomyId) {
		rf.taxonomyRequest().findOne(taxonomyId).fire(receiver);
	}
	
	public void findStats(Receiver<TaxonomyStatsProxy> receiver,Long taxonomyId) {
		rf.taxonomyRequest().findStats(taxonomyId).fire(receiver);
	}
	
}
