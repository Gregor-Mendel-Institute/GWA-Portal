package com.gmi.nordborglab.browser.shared.service;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.TraitUomService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface PhenotypeRequest extends RequestContext{
	Request<PhenotypePageProxy> findPhenotypesByExperiment(Long id,int start,int size);

	Request<PhenotypeProxy> findPhenotype(Long id);

	Request<PhenotypeProxy> save(PhenotypeProxy phenotype);
	
	Request<List<PhenotypeProxy>> findPhenotypesByPassportId(Long passportId);
	
	Request<PhenotypePageProxy> findAll(String name,String experiment,String ontology,String protocol,int start,int size);

    Request<List<PhenotypeProxy>> findPhenotypesByExperimentAndAcl(Long experimentId, int permission);

    Request<Long> savePhenotypeUploadData(Long experimentId,PhenotypeUploadDataProxy data);
}
