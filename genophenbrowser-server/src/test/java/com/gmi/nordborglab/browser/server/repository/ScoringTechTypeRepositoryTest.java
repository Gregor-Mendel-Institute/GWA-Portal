package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.genotype.ScoringTechType;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;

public class ScoringTechTypeRepositoryTest  extends BaseTest{
	
	@Resource
	protected ScoringTechTypeRepository repository;

	@Test
	public void testFindById() {
		ScoringTechType actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		ScoringTechType deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		ScoringTechType created = new ScoringTechType();
		created.setScoringTechGroup("test");
		created.setScoringTechType("test");
		ScoringTechType actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getScoringTechType());
		assertEquals("Common name is correct", "test",actual.getScoringTechGroup());
	}
}

