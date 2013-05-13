package com.gmi.nordborglab.browser.server.domain.ontology;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public enum TermRelationshipTypes implements RelationshipType{
    IS_A,PART_OF
}
