package com.gmi.nordborglab.browser.shared.service;


import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;
import java.util.Set;

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.ExperimentService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface ExperimentRequest extends RequestContext {


    Request<ExperimentPageProxy> findByAclAndFilter(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<ExperimentProxy> findExperiment(Long id);

    Request<ExperimentProxy> save(ExperimentProxy experiment);

    Request<List<ExperimentProxy>> findAllByAcl(Integer permission);

    Request<ExperimentProxy> addPublication(Long id, PublicationProxy publication);

    Request<PublicationPageProxy> getPublications(String searchString, int start, int size);

    Request<Set<ExperimentProxy>> findExperimentsByPublication(Long id);

    Request<PublicationProxy> findOnePublication(Long id);

    Request<Void> delete(ExperimentProxy experiment);
}
