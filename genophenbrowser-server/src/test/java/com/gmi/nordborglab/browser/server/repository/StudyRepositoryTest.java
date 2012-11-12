package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.common.collect.Iterables;

public class StudyRepositoryTest extends BaseTest{

	@Resource
	protected StudyRepository repository;
	
	@Test
	public void testFindById() {
		Study actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
		assertNotNull("Allele assay is not found",actual.getAlleleAssay());
		//assertNotNull("did not find attached Taxonomy",actual.get());
		//assertNotNull("did not find attached Collection",actual.getScoringTechType());
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Study deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Study created = new Study();
		created.setName("test");
		Study actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "test",actual.getName());
	}
	
	@Test
	public void testfindByPhenotypeId() {
		PageRequest pageRequest = new PageRequest(0, 50);
		Page<Study> page = repository.findByPhenotypeId(1L, pageRequest);
		assertNotNull(page);
		assertEquals(0,page.getNumber());
		assertNotNull(page.getContent());
		assertEquals(2, page.getContent().size());
		assertEquals(2,page.getTotalElements());
		Study study = page.getContent().get(0);
		assertEquals(new Long(1L),Iterables.get(study.getTraits(),0).getTraitUom().getId());
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		Study created = new Study();
		created.setName("test");
		StudyProtocol protocol = new StudyProtocol();
		protocol.setAnalysisMethod("test");
		created.setProtocol(protocol);
		Trait trait = new Trait();
		trait.setValue("test");
		created.addTrait(trait);
		AlleleAssay assay = new AlleleAssay();
		assay.setName("test");
		created.setAlleleAssay(assay);
		Study actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is correct", "test",actual.getName());
		assertNotNull("Allele Assay not set", actual.getAlleleAssay());
		assertEquals("wrong name for Allele Assay", "test",actual.getAlleleAssay().getName());
		assertNotNull("Protocol not set", actual.getProtocol());
		assertEquals("wrong analysis method for Protocol", "test",actual.getProtocol().getAnalysisMethod());
		assertNotNull("traits not set", actual.getTraits());
		assertEquals("correct trait count", 1,actual.getTraits().size());
		assertEquals("correct trait value", "test",Iterables.get(actual.getTraits(),0).getValue());
	}
	
	@Test
	public void testFindAllByPassportId() {
		List<Study> studies = repository.findAllByPassportId(6964L, new Sort("id"));
		assertNotNull("no studies returned",studies);
		assertEquals("wrong number of studies",2,studies.size());
		assertEquals("wrong study",1,studies.get(0).getId().longValue());
		assertEquals("wrong study",850,studies.get(1).getId().longValue());
	}
}

