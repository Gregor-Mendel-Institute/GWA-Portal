package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.10.13
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public class UserServiceTest extends BaseTest {

    @Resource
    private UserService service;

    @Resource
    private UserRepository userRepository;


    @Before
    public void setUp() {


    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void findOne() {
        SecurityUtils.setAnonymousUser();
        AppUser user = service.findUser(1L);
        assertNotNull(user);
    }

    @Test(expected = AccessDeniedException.class)
    public void saveUserByAnonymousNotAllowed() {
        SecurityUtils.setAnonymousUser();
        AppUser user = userRepository.findOne(1L);
        user.setFirstname("TEST");
        service.saveUser(user);
    }

    @Test()
    public void saveOwnUser() {
        SecurityUtils.makeActiveUser(1L, "1", "TEST", ImmutableList.of(new SimpleGrantedAuthority("ROLE_USER")).asList());
        AppUser user = userRepository.findOne(1L);
        user.setFirstname("TEST");
        service.saveUser(user);
        AppUser newUser = userRepository.findOne(1L);
        assertNotNull(newUser);
        assertEquals(newUser.getFirstname(), user.getFirstname());
    }

    @Test(expected = AccessDeniedException.class)
    public void saveNonAdminOtherUserNotAllowed() {
        SecurityUtils.makeActiveUser(1L, "1", "TEST", ImmutableList.of(new SimpleGrantedAuthority("ROLE_USER")).asList());
        AppUser user = userRepository.findOne(2L);
        user.setFirstname("TEST");
        service.saveUser(user);
    }

    @Test
    public void saveOtherUserByAdmin() {
        SecurityUtils.makeActiveUser(1L, "1", "TEST", ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList());
        AppUser user = userRepository.findOne(2L);
        user.setFirstname("TEST");
        service.saveUser(user);
        AppUser newUser = userRepository.findOne(2L);
        assertNotNull(newUser);
        assertEquals(newUser.getFirstname(), user.getFirstname());
    }
}
