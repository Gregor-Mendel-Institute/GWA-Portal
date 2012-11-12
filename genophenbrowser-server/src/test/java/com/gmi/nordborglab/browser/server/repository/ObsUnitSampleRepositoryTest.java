package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnitSample;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class ObsUnitSampleRepositoryTest extends BaseTest{
	
	@Resource
	protected ObsUnitSampleRepository repository;
	
	
	@Ignore("no data in table")
	@Test
	public void testFindById() {
		ObsUnitSample actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Ignore("no data in table")
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		ObsUnitSample deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		ObsUnitSample created = new ObsUnitSample();
		created.setName("test");
		ObsUnitSample actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getName());
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		ObsUnitSample created = new ObsUnitSample();
		created.setName("test");
		created.setObsUnit(ObsUnitRepositoryTest.createObsUnitWithAllDependencies());
		ObsUnitSample actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertEquals("name is incorrect","test",actual.getName());
		ObsUnitRepositoryTest.assertObsUnit(actual.getObsUnit());
	}
	
	

}
