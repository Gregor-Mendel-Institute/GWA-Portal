package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.genotype.Allele;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;

public class AlleleRepositoryTest extends BaseTest{

	@Resource
	protected AlleleRepository repository;
	
	@Test
	public void testFindById() {
		Allele actual = repository.findOne(3L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)3L, (double)actual.getId(),0L);
		assertNotNull("Allele assay is not found",actual.getAlleleAssay());
		//assertNotNull("did not find attached Taxonomy",actual.get());
		//assertNotNull("did not find attached Collection",actual.getScoringTechType());
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(3L);
		Allele deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Allele created = new Allele();
		created.setAccession("test");
		Allele actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "test",actual.getAccession());
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		Allele created = new Allele();
		created.setAccession("test");
		AlleleAssay assay = new AlleleAssay();
		assay.setName("test");
		created.setAlleleAssay(assay);
		Allele actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "test",actual.getAccession());
		assertNotNull("Allele Assay not set", actual.getAlleleAssay());
		assertEquals("wrong name for Allele Assay", "test",actual.getAlleleAssay().getName());
	}
}

