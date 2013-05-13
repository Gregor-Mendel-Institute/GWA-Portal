package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 */

@ServiceName(value="com.gmi.nordborglab.browser.server.service.GraphOntologyService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface GraphOntologyRequest extends RequestContext {

    Request<GraphTermProxy> findRootTerm(String type);

    Request<GraphTermProxy> findOneByAcc(String acc);

    Request<GraphTerm2TermProxy> findOneTerm2Term(Long id);
}
