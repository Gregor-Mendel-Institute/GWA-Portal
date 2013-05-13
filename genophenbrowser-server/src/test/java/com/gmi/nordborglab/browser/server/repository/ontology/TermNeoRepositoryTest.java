package com.gmi.nordborglab.browser.server.repository.ontology;

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

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/8/13
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class TermNeoRepositoryTest extends BaseTest {

    @Resource
    protected TermNeoRepository repository;

    @Resource
    protected Neo4jTemplate template;

    @Test
    public void testFindById() {
        Term actual = repository.findById("TO:0000166");
        template.fetch(actual.getIsAChildren());
        template.fetch(actual.getPartOfChildren());
        assertNotNull("did not find expected entity", actual);
        assertEquals("TO:0000166", actual.getId());
        assertEquals(2,actual.getIsAChildren().size());
        assertEquals(1,actual.getIsAParents().size());
    }

    @Test
    public void testFindShortestPath() {
        EndResult<EntityPath<Term,Term>> endResult = repository.findShortestPath("TO:0000166","TO:0000387");
        EntityPath<Term,Term> path = endResult.single();
        List<Long> idsInPath = Lists.newArrayList();
        for (Node node : path.nodes()) {
            idsInPath.add(node.getId());
        }
        long[] checkIds =  {909,784,1096,13,907,174};
        assertEquals(6,idsInPath.size());
        assertArrayEquals(checkIds, Longs.toArray(idsInPath));
        String test="test";
    }
}
