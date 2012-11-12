package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class TraitRepositoryTest extends BaseTest{
	
	@Resource
	protected TraitRepository repository;
	
	@Test
	public void testFindById() {
		Trait actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Trait deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Trait created = new Trait();
		created.setValue("test");
		Trait actual = repository.save(created);
		assertTrait(actual);
	}
	
	@Test
	public void testCreateWithAllRelationships() {
		Trait created = createTraitWithAllDependencies();
		Trait actual = repository.save(created);
		assertTrait(actual);
		TraitUomRepositoryTest.assertTraitUom(actual.getTraitUom());
	}
	
	@Test
	public void testFindAllTraitValues() {
		List<Trait> traits = repository.findAllTraitValues(1L,1L,null);
		assertNotNull("nothing returned",traits);
		assertEquals(334, traits.size());
	}
	
	@Test
	public void testFindAllTraitValuesByStatisticTypeAndAlleleAssay() {
		List<Trait> traits = repository.findAllTraitValues(1L,1L,2L);
		assertNotNull("nothing returned",traits);
		assertEquals(167, traits.size());
	}
	
	@Test
	public void testFindAllTraitValuesByStatisticType() {
		List<Trait> traits = repository.findByTraitUomIdAndStatisticTypeId(1L,2L);
		assertNotNull("nothing returned",traits);
		assertEquals(167, traits.size());
	}
	
	public static Trait createTraitWithAllDependencies() {
		Trait created = new Trait();
		created.setValue("test");
		TraitUom traitUom = TraitUomRepositoryTest.createTraitUomWithAllDependencies();
		created.setTraitUom(traitUom);
		StatisticType statisticType = new StatisticType();
		statisticType.setStatType("test");
		created.setStatisticType(statisticType);
		return created;
	}
	
	public static void assertTrait(Trait actual) {
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("value is incorrect", "test",actual.getValue());
	}

}
