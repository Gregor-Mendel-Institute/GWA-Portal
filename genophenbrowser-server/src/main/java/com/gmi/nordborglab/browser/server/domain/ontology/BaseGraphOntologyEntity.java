package com.gmi.nordborglab.browser.server.domain.ontology;

import org.springframework.data.neo4j.annotation.GraphId;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 6:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseGraphOntologyEntity {
    @GraphId
    private Long nodeId;

    public BaseGraphOntologyEntity() {
    }

    public Long getNodeId() {
        return nodeId;
    }
}
