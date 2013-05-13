package com.gmi.nordborglab.browser.server.domain.ontology;

import org.springframework.data.neo4j.annotation.*;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */


@RelationshipEntity
public class BasicTerm2Term extends BaseGraphOntologyEntity{



    @StartNode
    private Term child;
    @EndNode
    private Term parent;


    public BasicTerm2Term() {

    }


    public Term getChild() {
        return child;
    }

    public Term getParent() {
        return parent;
    }

    public String getType() {
        return "";
    }
}
