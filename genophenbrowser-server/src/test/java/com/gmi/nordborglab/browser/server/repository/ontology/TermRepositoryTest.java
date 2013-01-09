package com.gmi.nordborglab.browser.server.repository.ontology;

import static org.junit.Assert.*;


import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.jpaontology.model.Term;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;

public class TermRepositoryTest extends BaseTest{
	
	@Resource
	protected TermRepository repository;
	
	@Test
	public void testFindById() {
		Term actual = repository.findOne(1);
		assertNotNull("did not find expected entity", actual);
		assertEquals(new Integer(1), actual.getId());
	}
	
	@Test
	public void testFindByAcc() {
		Term actual = repository.findByAcc("TO:0000387");
		assertNotNull("did not find expected entity", actual);
		assertEquals("TO:0000387", actual.getAcc());
		assertTrue(actual.getIsRoot());
		assertEquals(0,actual.getParents().size());
		assertNotNull(actual.getChilds());
		assertEquals(9,actual.getChilds().size());
	}
	
	
	


}
