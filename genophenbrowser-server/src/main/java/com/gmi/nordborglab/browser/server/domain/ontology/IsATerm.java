package com.gmi.nordborglab.browser.server.domain.ontology;

import org.springframework.data.neo4j.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */

public class IsATerm extends BasicTerm2Term{

    private static String TYPE = "is_a";

    public IsATerm() {

    }


    @Override
    public String getType() {
        return TYPE;
    }
}
