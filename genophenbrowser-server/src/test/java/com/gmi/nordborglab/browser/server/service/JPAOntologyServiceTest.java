package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.gmi.nordborglab.jpaontology.model.Term;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.acls.model.MutableAclService;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/7/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JPAOntologyServiceTest extends BaseTest {

    @Resource
    private JPAOntologyService service;


    @Resource
    private MutableAclService aclService;

    @Before
    public void setUp() {
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void testFindOneByAcc() {
        Term term = service.findOneByAcc("TO:0000237");
        assertNotNull(term);
        assertNotNull(term.getPathToRoot());
        assertEquals(3,term.getPathToRoot().size());
        assertEquals(201,term.getPathToRoot().get(0).intValue());
        assertEquals(205,term.getPathToRoot().get(1).intValue());
        assertEquals(290,term.getPathToRoot().get(2).intValue());
    }
}