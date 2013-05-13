package com.gmi.nordborglab.browser.server.repository.ontology;

import com.gmi.nordborglab.browser.server.domain.ontology.BasicTerm2Term;
import com.gmi.nordborglab.browser.server.domain.ontology.Term;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.core.EntityPath;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/10/13
 * Time: 5:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicTerm2TermRepositoryTest extends BaseTest {

    @Resource
    protected BasicTerm2TermRepository repository;

    @Resource
    protected Neo4jTemplate template;

    @Test
    public void testFindOne() {
        BasicTerm2Term actual = repository.findOne(719L);
        assertNotNull("did not find expected entity", actual);
        assertEquals(719L, actual.getNodeId().longValue());
    }
}