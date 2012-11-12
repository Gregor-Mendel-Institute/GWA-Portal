package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.domain.observation.Locality;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;



public class TaxonomyRepositoryTest extends BaseTest{
	
	@Resource
	protected TaxonomyRepository repository;
	
	@Test
	public void testFindById() {
		Taxonomy actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Taxonomy deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Taxonomy created = new Taxonomy();
		created.setCommonName("test");
		Taxonomy actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getCommonName());
	}
	
	@Test
	public void testFindAlleleAssaysForTaxonomy() {
		List<AlleleAssay> alleleAssays = repository.findAlleleAssaysForTaxonomy(1L);
		assertNotNull("nothing found",alleleAssays);
		assertEquals("wrong number of Allele assays",1,alleleAssays.size());
		AlleleAssay a = alleleAssays.get(0);
		assertEquals(1L, a.getId().doubleValue(),0L);
	}
	
	@Test
	public void testCountPassportsPerSampStat() {
		List<Object[]>  stats = repository.countPassportsPerSampStat(1L);
		assertEquals("no stats found",1,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof Sampstat);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 7064L,stat[1]);
	}
	
	@Test
	public void testCountPassportsPerAlleleAssay() {
		List<Object[]>  stats = repository.countPassportsPerAlleleAssay(1L);
		assertEquals("no stats found",1,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof AlleleAssay);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 1386L,stat[1]);
	}
	
	@Test
	public void testCountPassportsPerCountry() {
		List<Object[]>  stats = repository.countPassportsPerCountry(1L);
		assertEquals("no stats found",45,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof String);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 1L,stat[1]);
	}
	
	@Test
	public void testCountStocksPerGeneration() {
		List<Object[]>  stats = repository.countStocksPerGeneration(1L);
		assertEquals("no stats found",3,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof String);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 16L,stat[1]);
	}

}
