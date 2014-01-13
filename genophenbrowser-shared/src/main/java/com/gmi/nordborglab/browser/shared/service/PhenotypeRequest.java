package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeUploadDataProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.TraitUomService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface PhenotypeRequest extends RequestContext {
    Request<PhenotypePageProxy> findPhenotypesByExperiment(Long id, int start, int size);

    Request<PhenotypeProxy> findPhenotype(Long id);

    Request<PhenotypeProxy> save(PhenotypeProxy phenotype);

    Request<List<PhenotypeProxy>> findPhenotypesByPassportId(Long passportId);

    Request<PhenotypePageProxy> findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<PhenotypePageProxy> findAll(Long id, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<List<PhenotypeProxy>> findPhenotypesByExperimentAndAcl(Long experimentId, int permission);

    Request<Long> savePhenotypeUploadData(Long experimentId, PhenotypeUploadDataProxy data);

    Request<List<PhenotypeProxy>> findAllByOntology(String type, String acc, boolean checkChilds);

    Request<Void> delete(PhenotypeProxy phenotype);
}
