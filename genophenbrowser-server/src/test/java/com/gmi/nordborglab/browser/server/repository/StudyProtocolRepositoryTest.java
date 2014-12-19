package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class StudyProtocolRepositoryTest extends BaseTest {

    @Resource
    protected StudyProtocolRepository repository;

    @Test
    public void testFindById() {
        StudyProtocol actual = repository.findOne(1L);
        assertNotNull("did not find expected entity", actual);
        assertEquals((double) 1L, (double) actual.getId(), 0L);
        assertThat(actual.getGwasRuntimeInfos(), is(notNullValue()));
        assertThat(actual.getGwasRuntimeInfos().size(), is(not(0)));
    }

    @Test
    public void testDeleteById() {
        repository.delete(1L);
        StudyProtocol deleted = repository.findOne(1L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        StudyProtocol created = new StudyProtocol();
        created.setAnalysisMethod("test");
        StudyProtocol actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("Common name is correct", "test", actual.getAnalysisMethod());
    }
}

