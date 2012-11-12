package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class SampstatRepositoryTest extends BaseTest {
	
	@Resource
	SampstatRepository repository;
	
	@Test
	public void testFindById() {
		Sampstat actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Sampstat deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Sampstat created = new Sampstat();
		created.setSampstat("test");
		created.setGermplasmType("test");
		Sampstat actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getSampstat());
		assertEquals("Common name is correct", "test",actual.getGermplasmType());
	}

}
