package com.gmi.nordborglab.browser.server.service;


import com.gmi.nordborglab.browser.server.domain.ontology.BasicTerm2Term;
import com.gmi.nordborglab.browser.server.domain.ontology.Term;
import com.gmi.nordborglab.browser.shared.proxy.ontology.GraphTerm2TermProxy;


/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/8/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GraphOntologyService {

    public Term findRootTerm(String type);
    public Term findOneByAcc(String acc);
    public BasicTerm2Term findOneTerm2Term(Long id);
}
