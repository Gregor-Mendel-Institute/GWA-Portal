package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.ObsUnitPage;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/7/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyServiceTest extends BaseTest {

    @Resource
    private OntologyService service;


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