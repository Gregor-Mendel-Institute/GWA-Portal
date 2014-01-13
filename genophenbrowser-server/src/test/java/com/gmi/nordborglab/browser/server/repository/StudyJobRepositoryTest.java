package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/12/13
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class StudyJobRepositoryTest extends BaseTest {

    @Resource
    protected StudyJobRepository repository;

    @Test
    public void testFindById() {
        StudyJob studyJob = repository.findOne(8L);
        assertNotNull(studyJob);
    }

    @Test
    public void testDeleteById() {
        repository.delete(8L);
        StudyJob deleted = repository.findOne(8L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        StudyJob created = new StudyJob();
        created.setTaskid("TEST");
        StudyJob actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("name is correct", "TEST", actual.getTaskid());
    }
}


