package com.gmi.nordborglab.browser.server.repository.ontology;

import com.gmi.nordborglab.browser.server.domain.ontology.BasicTerm2Term;
import com.gmi.nordborglab.browser.server.domain.ontology.Term;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.core.EntityPath;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/8/13
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */


public interface TermNeoRepository extends GraphRepository<Term> {

    //@Query("START term=node:ontologyid(id={0}) RETURN term")
    public Term findById(String id);

    @Query("START n=node:Term(id={0}),m =node:Term(id={1}) match p = shortestPath(n-[*]->m) RETURN p")
    public EndResult<EntityPath<Term,Term>> findShortestPath(String start,String end);
}
