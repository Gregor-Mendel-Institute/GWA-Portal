package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.ontology.Term2TermProxy;
import com.gmi.nordborglab.browser.shared.proxy.ontology.TermProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */

@ServiceName(value="com.gmi.nordborglab.browser.server.service.JPAOntologyService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface OntologyRequest extends RequestContext{
    Request<TermProxy> findOne(Integer id);

    Request<Term2TermProxy> findOneTerm2Term(Integer id);

    Request<TermProxy> findRootTerm(String type);

    Request<TermProxy> findOneByAcc(String acc);
}
