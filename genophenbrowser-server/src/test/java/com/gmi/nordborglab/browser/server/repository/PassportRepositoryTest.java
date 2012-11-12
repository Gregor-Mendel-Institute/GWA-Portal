package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.cdv.Source;
import com.gmi.nordborglab.browser.server.domain.germplasm.AccessionCollection;
import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class PassportRepositoryTest extends BaseTest{

	@Resource
	protected PassportRepository repository;
	
	@Test
	public void testFindById() {
		Passport passport = repository.findOne(1L);
		assertNotNull("did not find expected entity", passport);
		assertEquals((double)1L, (double)passport.getId(),0L);
		assertNotNull("did not find attached Taxonomy",passport.getTaxonomy());
		assertNotNull("did not find attached Collection",passport.getCollection());
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Passport deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Passport created = new Passport();
		created.setAccename("test");
		Passport actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "test",actual.getAccename());
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		Passport created = new Passport();
		created.setAccename("test");
		AccessionCollection collection = new AccessionCollection();
		created.setCollection(collection);
		Taxonomy taxonomy = new Taxonomy();
		taxonomy.setCommonName("test");
		Source source = new Source();
		source.setSource("TEST");
		created.setSource(source);
		created.setTaxonomy(taxonomy);
		Passport actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "test",actual.getAccename());
		assertNotNull("Taxonomy is not set", actual.getTaxonomy());
		assertNotNull("Collecting is not set",actual.getCollection());
		assertNotNull("Source is not set",actual.getSource());
		assertEquals("wrong source for Source","TEST",actual.getSource().getSource());
		assertEquals("wrong CommonName for Taxonomy", "test",actual.getTaxonomy().getCommonName());
	}
	
	@Test
	public void testCountPassportsPerTraitOntology() {
		List<Object[]>  stats = repository.countPassportsPerTraitOntology(1L);
		assertEquals("no stats found",5,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof String);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 2L,stat[1]);
	}
	@Test
	public void testCountPassportsPerEnvironmentOntology() {
		List<Object[]>  stats = repository.countPassportsPerEnvironmentOntology(1L);
		assertEquals("no stats found",1,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		//assertTrue("wrong key",stat[0] instanceof String);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 388L,stat[1]);
	}
	@Test
	public void testCountPassportsPerStatisticType() {
		List<Object[]>  stats = repository.countPassportsPerStatisticType(1L);
		assertEquals("no stats found",3,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof StatisticType);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 42L,stat[1]);
	}
	@Test
	public void testCountPassportsPerUnitOfMeasure() {
		List<Object[]>  stats = repository.countPassportsPerUnitOfMeasure(5837L);
		assertEquals("no stats found",2,stats.size());
		Object[] stat = stats.get(0);
		assertEquals("wrong number of elements", 2,stat.length);
		assertTrue("wrong key",stat[0] instanceof UnitOfMeasure);
		assertTrue("wrong value",stat[1] instanceof Long);
		assertEquals("wrong number of passports", 10L,stat[1]);
	}
}
