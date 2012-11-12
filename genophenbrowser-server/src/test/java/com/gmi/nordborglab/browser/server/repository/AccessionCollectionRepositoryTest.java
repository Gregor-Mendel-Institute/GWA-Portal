package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.germplasm.AccessionCollection;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;

public class AccessionCollectionRepositoryTest extends BaseTest {
	
	@Resource
	protected AccessionCollectionRepository repository;
	
	@Test
	public void testFindById() {
		AccessionCollection actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		AccessionCollection deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		AccessionCollection created = new AccessionCollection();
		created.setCollNumb("test");
		AccessionCollection actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getCollNumb());
	}

}
