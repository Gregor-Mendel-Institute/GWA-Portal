package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static com.gmi.nordborglab.browser.server.domain.specifications.AppUserSpecifications.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.specifications.AppUserSpecifications;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class UserRepositoryTest extends BaseTest {

    @Resource
    protected UserRepository repository;

    @Test
    public void testFindByUsername() {
        AppUser actual = repository.findOne(16L);
        assertNotNull("did not find expected entity", actual);
        assertEquals("username is not equal", actual.getUsername(), "john");
    }

    @Test
    public void testDeleteByUsername() {
        repository.delete(16L);
        AppUser deleted = repository.findOne(16L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        AppUser created = new AppUser("test");
        List<Authority> authorities = new ArrayList<Authority>();
        Authority authority = new Authority();
        authority.setAuthority("ROLE_USER");
        authorities.add(authority);
        created.setAuthorities(authorities);
        AppUser actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertEquals("username is correct", "test", actual.getUsername());
        assertNotNull("authorites is empty", actual.getAuthorities());
        assertEquals(1L, actual.getAuthorities().size());
        assertEquals("ROLE_USER", actual.getAuthorities().get(0).getAuthority());
        assertEquals(actual, actual.getAuthorities().get(0).getUser());
    }

    @Test
    public void testFindByFirstName() {
        List<AppUser> users = repository.findAll(firstNameIsLike("Fer"));
        assertEquals(1, users.size());
        assertEquals("Fernando", users.get(0).getFirstname());
    }

    @Test
    public void testFindByLastName() {
        List<AppUser> users = repository.findAll(lastNameIsLike("Rab"));
        assertEquals(1, users.size());
        assertEquals("Rabanal", users.get(0).getLastname());
    }

}
