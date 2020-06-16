package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ExperimentRepositoryTest extends BaseTest {

    @Resource
    protected ExperimentRepository repository;


    @Test
    public void testFindById() {
        Experiment actual = repository.findOne(1L);
        assertNotNull("did not find expected entity", actual);
        assertEquals((double) 1L, (double) actual.getId(), 0L);
    }

    @Test
    public void testDeleteById() {
        repository.delete(1L);
        Experiment deleted = repository.findOne(1L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        Experiment created = new Experiment();
        created.setName("test");
        Experiment actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("Common name is correct", "test", actual.getName());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testCreateConstraintViolation() {
        Experiment created = new Experiment();
        created.setName("");
        Experiment actual = repository.save(created);
        String test = "test";
    }


    @Test
    public void findExperimentByPhenotypeId() {
        Experiment experiment = repository.findByPhenotypeId(1L);
        assertNotNull(experiment);
        assertEquals(new Long(1L), experiment.getId());
    }


}
