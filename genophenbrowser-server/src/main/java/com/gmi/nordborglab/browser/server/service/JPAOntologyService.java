package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.model.Term2Term;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/6/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */


public interface JPAOntologyService {

    public Term findOne(Integer id);
    public Term2Term findOneTerm2Term(Integer id);
    public Term findRootTerm(String type);
    public Term findOneByAcc(String acc);
}
