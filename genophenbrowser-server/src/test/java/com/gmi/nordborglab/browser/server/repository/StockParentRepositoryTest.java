package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.germplasm.StockParent;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class StockParentRepositoryTest extends BaseTest{
	
	@Resource
	StockParentRepository repository;
	
	@Test
	public void testFindById() {
		StockParent actual = repository.findOne(2L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)2L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(2L);
		StockParent deleted = repository.findOne(2L);
		assertNull("delete did not work", deleted);
	}
	
	

	
	@Test
	public void testCreateWithAllRelationships() {
		Stock child = new Stock();
		child.setStockSource("child");
		Stock father = new Stock();
		father.setStockSource("father");
		StockParent created = new StockParent();
		created.setChild(child);
		created.setParent(father);
		created.setRole("father");
		StockParent actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "father",actual.getRole());
		assertNotNull("Parent is not set", actual.getParent());
		assertEquals("Wrong stock source for parent","father",actual.getParent().getStockSource());
		assertNotNull("Child is not set",actual.getChild());
		assertEquals("Wrong stock source for child","child",actual.getChild().getStockSource());
		assertNotNull("parents not set in child",actual.getChild().getParents());
		assertNotNull("children not set in child",actual.getParent().getChilds());
	}
}
