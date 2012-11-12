package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.observation.Locality;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class LocalityRepositoryTest extends BaseTest{
	
	@Resource
	protected LocalityRepository repository;

	@Test
	public void testFindById() {
		Locality actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Locality deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Locality created = new Locality();
		created.setLocalityName("test");
		Locality actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getLocalityName());
	}

}
