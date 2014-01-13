package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.common.collect.Iterables;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformationRepositoryTest extends BaseTest {

    @Resource
    protected TransformationRepository repository;

    @Test
    public void testFindById() {
        Transformation actual = repository.findOne(1L);
        assertNotNull("did not find expected entity", actual);
        assertEquals(1, actual.getId().longValue());
        assertNotNull(actual.getStudies());
        assertTrue(actual.getStudies().size() > 0);
        assertEquals(Iterables.get(actual.getStudies(), 0).getTransformation(), actual);
    }

}