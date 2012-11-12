package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.acls.domain.BasePermission;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;

public class ExperimentRepositoryTest extends BaseTest{
	
	@Resource
	protected ExperimentRepository repository;
	

	@Test
	public void testFindById() {
		Experiment actual = repository.findOne(1L);
		assertNotNull("did not find expected entity", actual);
		assertEquals((double)1L, (double)actual.getId(),0L);
	}
	
	@Test
	public void testDeleteById() {
		repository.delete(1L);
		Experiment deleted = repository.findOne(1L);
		assertNull("delete did not work", deleted);
	}
	
	@Test
	public void testCreate() {
		Experiment created = new Experiment();
		created.setName("test");
		Experiment actual = repository.save(created);
		assertNotNull("create did not work",actual);
		assertNotNull("couldn't generate id",actual.getId());
		assertEquals("Common name is correct", "test",actual.getName());
	}
	
	@Test
	public void findByAdministrator() {
		List<String> permissions = new ArrayList<String>();
		permissions.add("ROLE_ADMIN");
		permissions.add("ROLE_USER");
		permissions.add("ROLE_ANONYMOUS");
		Page<Experiment> page = repository.findByAcl(permissions,BasePermission.READ.getMask(),new PageRequest(0, 5));
		assertNotNull(page);
		assertEquals(14, page.getTotalElements());
		assertEquals(5,page.getContent().size());
		assertEquals(0,page.getNumber());
		assertEquals(5,page.getNumberOfElements());
	}
	
	@Ignore
	@Test
	public void findByUserRole() {
		List<String> permissions = new ArrayList<String>();
		permissions.add("ROLE_USER");
		permissions.add("ROLE_ANONYMOUS");
		Page<Experiment> page = repository.findByAcl(permissions,BasePermission.READ.getMask(),new PageRequest(0, 5));
		assertNotNull(page);
		assertEquals(2, page.getTotalElements());
		assertEquals(2,page.getContent().size());
		assertEquals(0,page.getNumber());
		assertEquals(2,page.getNumberOfElements());
		assertEquals(1, page.getContent().get(0).getId().intValue());
		assertEquals(2, page.getContent().get(1).getId().intValue());
	}
	
	@Ignore
	@Test
	public void findByAnonymousUser() {
		List<String> permissions = new ArrayList<String>();
		permissions.add("ROLE_ANONYMOUS");
		Page<Experiment> page = repository.findByAcl(permissions,BasePermission.READ.getMask(),new PageRequest(0, 5));
		assertNotNull(page);
		assertEquals(1, page.getTotalElements());
		assertEquals(1,page.getContent().size());
		assertEquals(0,page.getNumber());
		assertEquals(1,page.getNumberOfElements());
		assertEquals(3, page.getContent().get(0).getId().intValue());
	}
	
	@Test
	public void findExperimentByPhenotypeId() {
		Experiment experiment = repository.findByPhenotypeId(1L);
		assertNotNull(experiment);
		assertEquals(new Long(1L),experiment.getId());
	}
}
