package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.gmi.nordborglab.browser.server.domain.TaxonomyStats;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import com.gmi.nordborglab.browser.server.repository.TaxonomyRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;
import com.google.visualization.datasource.datatable.DataTable;

public class TaxonomyServiceTest extends BaseTest {

	@Resource
	private TaxonomyService service;

	@Resource
	private TaxonomyRepository repository;

	@Before
	public void setUp() {
	}

	@After
	public void clearContext() {
		SecurityUtils.clearContext();
	}
	
	@Test
	public void testFindAll() {
		List<Taxonomy> taxonomies = service.findAll();
		assertNotNull("nothing returned",taxonomies);
		assertEquals("Wrong number returned",2, taxonomies.size());
		Taxonomy taxonomy  = taxonomies.get(0);
		assertNotNull("no allele Assay returned",taxonomy.getAlleleAssays());
		assertEquals("wrong number of allele assays",1, taxonomy.getAlleleAssays().size());
	}
	
	@Test
	public void testFindOne() {
		Taxonomy taxonomy = service.findOne(1L);
		assertNotNull("nothing returned", taxonomy);
		assertEquals("wrong id", 1L,taxonomy.getId().longValue());
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testSaveAndAccessDeniedException() {
		SecurityUtils.setAnonymousUser();
		Taxonomy taxonomy = repository.findOne(1L);
		taxonomy.setGenus("TEST");
		service.save(taxonomy);
	}
	
	@Test()
	public void testSave() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
		Taxonomy taxonomy = repository.findOne(1L);
		taxonomy.setGenus("TEST");
		Taxonomy savedTaxonomy = service.save(taxonomy);
		assertEquals("TEST", savedTaxonomy.getGenus());
	}
	
	@Test
	public void testFindStats() {
		TaxonomyStats stats = service.findStats(1L);
		assertNotNull(stats);
		assertNotNull(stats.getAlleleAssayData());
		assertNotNull(stats.getGeoChartData());
		assertNotNull(stats.getSampStatData());
		assertNotNull(stats.getStockGenerationData());
	}

}

