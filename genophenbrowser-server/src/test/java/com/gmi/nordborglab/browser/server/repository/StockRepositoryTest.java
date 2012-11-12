package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.data.domain.Sort;

import com.gmi.nordborglab.browser.server.domain.germplasm.Generation;
import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class StockRepositoryTest extends BaseTest{
	
	@Resource
	protected StockRepository repository;

	@Test
	public void testFindById() {
		Stock actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Stock deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Stock created = new Stock();
		created.setStockSource("test");
		Stock actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getStockSource());
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		Stock created = new Stock();
		created.setStockSource("test");
		Generation generation = new Generation();
		generation.setIcisId("test");
		created.setGeneration(generation);
		Stock actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertNotNull("Generation is not set", actual.getGeneration());
		assertEquals("Icis of Generation is not correct", "test",actual.getGeneration().getIcisId());
	}
	
	@Test
	public void testFindAllByPassportId() {
		Sort sort = new Sort("id");
		List<Stock> stocks = repository.findAllByPassportId(1L,sort);
		assertNotNull("No stocks found",stocks);
		assertEquals("wrong number of stocks",1,stocks.size());
		Stock stock = stocks.get(0);
		assertEquals("wrong associated passport",1,stock.getPassport().getId().longValue());
	}
	
	@Test
	public void testFindAncestors() {
		List<Object[]> ancestors = repository.findAncestors(100657L);
		assertNotNull("ancestors not found",ancestors);
		assertEquals("wrong number of ancestors",6,ancestors.size());
		Object[] ancestor = ancestors.get(0);
		assertEquals("wrong number of paremeters",5,ancestor.length);
		assertTrue(ancestor[0] instanceof Integer);
		assertTrue(ancestor[1] instanceof Integer);
		assertTrue(ancestor[2] instanceof Integer);
		assertTrue(ancestor[3] instanceof String);
		assertTrue(ancestor[4] instanceof Short);
	}
	@Test
	public void testDscendents() {
		List<Object[]> descendents = repository.findDescendents(1L);
		assertNotNull("ancestors not found",descendents);
		assertEquals("wrong number of ancestors",6,descendents.size());
		Object[] ancestor = descendents.get(0);
		assertEquals("wrong number of paremeters",5,ancestor.length);
		assertTrue(ancestor[0] instanceof Integer);
		assertTrue(ancestor[1] instanceof Integer);
		assertTrue(ancestor[2] instanceof Integer);
		assertTrue(ancestor[3] instanceof String);
		assertTrue(ancestor[4] instanceof Short);
		
	}
}
