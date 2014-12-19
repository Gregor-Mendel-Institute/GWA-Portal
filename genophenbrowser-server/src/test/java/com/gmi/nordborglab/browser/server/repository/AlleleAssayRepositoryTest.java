package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.genotype.PolyType;
import com.gmi.nordborglab.browser.server.domain.genotype.ScoringTechType;
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

public class AlleleAssayRepositoryTest extends BaseTest {

    @Resource
    protected AlleleAssayRepository repository;

    @Test
    public void testFindById() {
        AlleleAssay actual = repository.findOne(1L);
        assertNotNull("did not find expected entity", actual);
        assertEquals((double) 1L, (double) actual.getId(), 0L);
        assertNotNull("did not find attached Taxonomy", actual.getPolyType());
        assertNotNull("did not find attached Collection", actual.getScoringTechType());
        assertThat(actual.getGwasRuntimeInfos(), is(notNullValue()));
        assertThat(actual.getGwasRuntimeInfos().size(), is(not(0)));
    }

    @Test
    public void testDeleteById() {
        repository.delete(1L);
        AlleleAssay deleted = repository.findOne(1L);
        assertNull("delete did not work", deleted);
    }

    @Test
    public void testCreate() {
        AlleleAssay created = new AlleleAssay();
        created.setName("test");
        AlleleAssay actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("name is correct", "test", actual.getName());
    }

    @Test
    public void testCreateWithAllRelationships() {
        AlleleAssay created = new AlleleAssay();
        created.setName("test");
        PolyType polyType = new PolyType();
        polyType.setPolyType("test");
        created.setPolyType(polyType);
        ScoringTechType scoringType = new ScoringTechType();
        scoringType.setScoringTechGroup("test");
        created.setScoringTechType(scoringType);
        AlleleAssay actual = repository.save(created);
        assertNotNull("create did not work", actual);
        assertNotNull("couldn't generate id", actual.getId());
        assertEquals("name is correct", "test", actual.getName());
        assertNotNull("Taxonomy is not set", actual.getScoringTechType());
        assertNotNull("Collecting is not set", actual.getPolyType());
        assertEquals("wrong CommonName for Taxonomy", "test", actual.getScoringTechType().getScoringTechGroup());
        assertEquals("wrong CommonName for Taxonomy", "test", actual.getPolyType().getPolyType());
    }

    @Test
    public void testFindAlleleAssayWithStats() {
        Long count = repository.countAvailableAlleles(1L, 2L, 1L);
        assertEquals(167, count.intValue());
        count = repository.countAvailableAlleles(1L, 2L, 2L);
        assertEquals(0, count.intValue());
    }
}
