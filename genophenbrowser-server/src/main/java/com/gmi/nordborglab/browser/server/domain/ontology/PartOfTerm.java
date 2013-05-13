package com.gmi.nordborglab.browser.server.domain.ontology;

import org.springframework.data.neo4j.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */

public class PartOfTerm extends BasicTerm2Term {

    private static String TYPE = "part_of";

    @Override
    public String getType() {
        return TYPE;
    }
}
