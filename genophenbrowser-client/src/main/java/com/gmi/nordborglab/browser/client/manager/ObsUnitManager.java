package com.gmi.nordborglab.browser.client.manager;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.ObsUnitPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.ObsUnitRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class ObsUnitManager extends RequestFactoryManager<ObsUnitRequest> {

	@Inject
	public ObsUnitManager(CustomRequestFactory rf) {
		super(rf);
	}

	@Override
	public ObsUnitRequest getContext() {
		return rf.obsUnitRequest();
	}
	
	public void findObsUnitsByPhenotypeId(Receiver<ObsUnitPageProxy> receiver,Long id,int start,int size) {
		getContext().findObsUnits(id, start, size).with("content.stock.passport.collection.locality").fire(receiver);
	}
	
	public void findObsUnitsWithNoGenotype(Receiver<List<ObsUnitProxy>> receiver,Long phenotypeId,Long alleleAssayId) {
		getContext().findObsUnitWithNoGenotype(phenotypeId, alleleAssayId).with("stock.passport.collection.locality").fire(receiver);
	}

}
