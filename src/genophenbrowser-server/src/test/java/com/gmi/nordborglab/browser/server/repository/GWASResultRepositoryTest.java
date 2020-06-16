package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 5:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASResultRepositoryTest extends BaseTest {

    @Resource
    GWASResultRepository repository;

    @Resource
    UserRepository userRepository;

    @Test
    public void testFindById() {
        GWASResult actual = repository.findOne(1L);
        assertNotNull("did not find expected entity", actual);
        assertEquals((double) 1L, (double) actual.getId(), 0L);
    }

    @Test
    public void testDeleteById() {
        repository.delete(1L);
        GWASResult deleted = repository.findOne(1L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        GWASResult created = new GWASResult();
        AppUser user = userRepository.findOne(1L);
        created.setName("TEST");
        created.setType("TEST");
        created.setComments("TEST");
        GWASResult actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("Common name is correct", "TEST", actual.getName());
        assertEquals("TEST", actual.getType());
        assertEquals("TEST", actual.getComments());
        assertNotNull(actual.getOwnerUser());
        assertEquals("john", actual.getOwnerUser().getUsername());

    }

}