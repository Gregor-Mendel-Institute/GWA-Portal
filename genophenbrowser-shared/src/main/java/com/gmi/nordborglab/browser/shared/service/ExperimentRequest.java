package com.gmi.nordborglab.browser.shared.service;


import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.ExperimentService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface ExperimentRequest extends RequestContext {

	Request<ExperimentPageProxy> findByAcl(int start, int size);
	Request<ExperimentProxy> findExperiment(Long id);
	Request<ExperimentProxy> save(ExperimentProxy experiment);
    Request<List<ExperimentProxy>> findAllByAcl(Integer permission);

    Request<ExperimentProxy> addPublication(Long id, PublicationProxy publication);
}
