package com.gmi.nordborglab.browser.client.manager;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class PhenotypeManager extends RequestFactoryManager<PhenotypeRequest> {

	@Inject
	public PhenotypeManager(CustomRequestFactory rf) {
		super(rf);
	}
	
	public void findAll(Receiver<PhenotypePageProxy> receiver,Long id,int start,int size) {
		rf.phenotypeRequest().findPhenotypesByExperiment(id, start, size).fire(receiver);
	}

	@Override
	public PhenotypeRequest getContext() {
		return rf.phenotypeRequest();
	}

	public void findOne(Receiver<PhenotypeProxy> receiver, Long id) {
		rf.phenotypeRequest().findPhenotype(id).with("statisticTypes","unitOfMeasure","userPermission").fire(receiver);
	}
	
	public void findAllTraitValues(Receiver<List<TraitProxy>> receiver,Long phenotypeId,Long alleleAssayId ,Long statisticTypeId) {
		rf.traitRequest().findAllTraitValues(phenotypeId, alleleAssayId, statisticTypeId).with("obsUnit.stock.passport.collection.locality","statisticType").fire(receiver);
	}

	public void findAllTraitValuesByType(Long phenotypeId,Long statisticTypeId,
			Receiver<List<TraitProxy>> receiver) {
		rf.traitRequest().findAllTraitValuesByStatisticType(phenotypeId,statisticTypeId).with("obsUnit.stock.passport.collection.locality").fire(receiver);
	}

}
