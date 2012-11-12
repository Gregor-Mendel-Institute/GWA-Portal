package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class ExperimentManager extends RequestFactoryManager<ExperimentRequest>{
	

	@Inject
	public ExperimentManager(CustomRequestFactory rf) {
		super(rf);
	}

	public void findAll(Receiver<ExperimentPageProxy> receiver,int start,int size) {
		rf.experimentRequest().findByAcl(start,size).with("content.acl").fire(receiver);
	}
	
	public void findOne(Receiver<ExperimentProxy> receiver,Long id) {
		rf.experimentRequest().findExperiment(id).with("userPermission").fire(receiver);
	}
	
	public CustomRequestFactory getRequestFactory() {
		return rf;
	}
	
	@Override
	public ExperimentRequest getContext() {
		return rf.experimentRequest();
	}

}
