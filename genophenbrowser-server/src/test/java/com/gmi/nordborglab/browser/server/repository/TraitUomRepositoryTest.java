package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.data.domain.Sort;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.gwt.thirdparty.guava.common.collect.Iterables;


public class TraitUomRepositoryTest extends BaseTest {
	
	@Resource
	protected TraitUomRepository repository;
	
	@Resource
	protected StudyRepository studyRepository;
	
	@Test
	public void testFindById() {
		TraitUom actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		TraitUom deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		TraitUom created = new TraitUom();
		created.setLocalTraitName("test");
		TraitUom actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("local trait name is incorrect", "test",actual.getLocalTraitName());
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		TraitUom created = createTraitUomWithAllDependencies();
		TraitUom actual = repository.save(created);
		assertTraitUom(actual);
	}
	
	@Test
	public void testRetrieveAllByExperiment() {
		List<TraitUom> traits = repository.findByExperimentId(1L);
		assertEquals(107, traits.size());
	}
	
	@Test
	public void testCountObsUnitsByPhenotypeId() {
		Long count = repository.countObsUnitsByPhenotypeId(1L);
		assertEquals(new Long(167L), count);
	}
	
	@Test
	public void testCountStudiesByPhenotypeId() {
		Long count = repository.countStudiesByPhenotypeId(1L);
		assertEquals(new Long(2), count);
	}
	
	@Test
	public void testFindByStudyId() {
		TraitUom trait = repository.findByStudyId(1L);
		assertNotNull("trait not found",trait);
		assertEquals("id of trait is wrong",1L, trait.getId().longValue());
	}
	
	@Test
	public void testCountTraitsForStatisticType() {
		List<Object[]> statisticTypes = repository.countTraitsForStatisticType(1L);
		assertNotNull("staticTypes map not found",statisticTypes);
		assertEquals("no statiticTypes found",2,statisticTypes.size());
		Object[] statisticType = statisticTypes.get(0);
		assertEquals("wrong number of elements", 2,statisticType.length);
		assertTrue("wrong key",statisticType[0] instanceof StatisticType);
		assertTrue("wrong value",statisticType[1] instanceof Long);
		assertEquals("wrong number of traits", 167L,statisticType[1]);
		
	}
	
	public static TraitUom createTraitUomWithAllDependencies() {
		TraitUom created = new TraitUom();
		created.setLocalTraitName("test");
		UnitOfMeasure unitOfMeasure = new UnitOfMeasure();
		unitOfMeasure.setUnitType("test");
		created.setUnitOfMeasure(unitOfMeasure);
		return created;
	}
	
	public static void assertTraitUom(TraitUom actual) {
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("name is incorrect", "test",actual.getLocalTraitName());
		assertNotNull("Experiment is not set", actual.getUnitOfMeasure());
		assertEquals("unit type for UnitOfMeasure incorrect", "test",actual.getUnitOfMeasure().getUnitType());
	}
	

	@Test
	public void testFindAllByPassportId() {
		List<TraitUom> traits = repository.findAllByPasportId(1L, new Sort("id"));
		assertEquals(254, traits.size());
	}
	
	@Test
	public void testFindAllByStudies() {
		List<Study> studies = new ArrayList<Study>();
		studies.add(studyRepository.findOne(1L));
		List<TraitUom> traits = repository.findAllByStudies(studies);
		assertNotNull("no traits found",traits);
		assertEquals("wrong number of traits",1,traits.size());
		assertEquals("wrong trait",1,Iterables.get(traits,0).getId().longValue());
	}
	
	@Test
	public void testFindAllByStudiesGrouped() {
		List<Study> studies = new ArrayList<Study>();
		studies.add(studyRepository.findOne(1L));
		List<Object[]> traits = repository.findAllByStudiesGrouped(studies);
		assertNotNull("no traits found",traits);
		assertEquals("wrong number of elements",1,traits.size());
		Object[] trait = traits.get(0);
		assertEquals("wrong number of elements", 2,trait.length);
		assertTrue("wrong key",trait[0] instanceof Study);
		assertTrue("wrong value",trait[1] instanceof TraitUom);
		assertEquals("wrong study", 1,((Study)trait[0]).getId().longValue());
		assertEquals("wrong trait", 1,((TraitUom)trait[1]).getId().longValue());
	}
}
