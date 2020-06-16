package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;
import com.gmi.nordborglab.jpaontology.model.TermPage;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */


public interface OntologyService {

    public Term findOne(Integer id);

    public Term2Term findOneTerm2Term(Integer id);

    public Term findRootTerm(String type);

    public Term findOneByAcc(String acc);

    public TermPage findByQuery(String query, ConstEnums.ONTOLOGY_TYPE type, int limit);

    public Set<Term> findAllByAcc(Set<String> accs);
}
