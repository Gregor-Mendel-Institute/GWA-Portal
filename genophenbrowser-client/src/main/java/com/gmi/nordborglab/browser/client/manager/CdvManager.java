package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.service.CdvRequest;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class CdvManager extends RequestFactoryManager<CdvRequest> {

	@Inject
	public CdvManager(CustomRequestFactory rf) {
		super(rf);
	}
	
	@Override
	public CdvRequest getContext() {
		return rf.cdvRequest();
	}
	
	public void findStudiesByPhenotypeId(Receiver<StudyPageProxy> receiver,Long id,int start,int size) {
		getContext().findStudiesByPhenotypeId(id, start, size).with("content.alleleAssay","content.protocol").fire(receiver);
	}

	public void findOne(Receiver<StudyProxy> receiver, Long id) {
		getContext().findStudy(id).with("traits.obsUnit.stock.passport.collection.locality","alleleAssay","protocol","userPermission").fire(receiver);
		
	}


}
