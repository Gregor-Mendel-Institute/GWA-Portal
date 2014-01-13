package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/18/13
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserNotificationRepositoryTest extends BaseTest {

    @Resource
    protected UserNotificationRepository repository;

    @Test
    public void testFindById() {
        UserNotification actual = repository.findOne(50L);
        assertNotNull("did not find expected entity", actual);
        assertEquals((double) 50L, (double) actual.getId(), 0L);
    }

    @Test
    public void testDeleteById() {
        repository.delete(50L);
        UserNotification deleted = repository.findOne(50L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        UserNotification created = new UserNotification();
        created.setType("test");
        created.setText("test");
        UserNotification actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("test", actual.getType());
        assertEquals("test", actual.getText());
    }

    @Test
    public void findByAppUserInOrderByIdDesc() {
        Long id = 16L;
        List<UserNotification> notifications = repository.findByAppUserIdOrAppUserIsNullOrderByIdDesc(id);
        assertNotNull(notifications);
        assertEquals(14, notifications.size());
    }


}
