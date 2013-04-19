package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.List;

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
        assertNotNull("create did not work",actual);
        assertNotNull("couldn't generate id",actual.getId());
        assertEquals("name is correct", "TEST",actual.getTaskid());
    }
}


