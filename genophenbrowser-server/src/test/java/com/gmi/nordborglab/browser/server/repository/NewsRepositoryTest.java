package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.NewsItem;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
public class NewsRepositoryTest extends BaseTest {

    @Resource
    protected NewsRepository repository;

    @Test
    public void testFindById() {
        NewsItem actual = repository.findOne(1L);
        assertNotNull("did not find expected entity", actual);
        assertEquals((double) 1L, (double) actual.getId(), 0L);
    }

    @Test
    public void testDeleteById() {
        repository.delete(1L);
        NewsItem deleted = repository.findOne(1L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        NewsItem created = new NewsItem();
        created.setTitle("TEST");
        NewsItem actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("Title is not correct", "TEST", actual.getTitle());
    }

}
