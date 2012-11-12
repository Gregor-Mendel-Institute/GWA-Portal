package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.acl.AclExperimentClass;
import com.gmi.nordborglab.browser.server.domain.acl.AclExperimentEntry;
import com.gmi.nordborglab.browser.server.domain.acl.AclExperimentIdentity;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;

public class AclExperimentReposistoryTest extends BaseTest {

	@Resource
	protected AclExperimentIdentityRepository aclRepository;
	
	@Resource 
	protected ExperimentRepository experimentRepository;	
	
	@Test
	public void testFindAclById() {
		AclExperimentIdentity experimentIdentity = aclRepository.findOne(2L);
		assertNotNull("did not find expected entity", experimentIdentity);
		assertEquals((double)2L, (double)experimentIdentity.getId(),0L);
		Experiment experiment = experimentIdentity.getExperiment();
		assertNotNull("did not find attached Experiment",experiment);
		assertNotNull("did not find attached Class",experimentIdentity.getAclClass());
		assertTrue(experimentIdentity.getAclClass() instanceof AclExperimentClass);
		assertNotNull("did not find attached Collection",experimentIdentity.getOwner());
	}
	
	@Test
	public void testFindAcls() {
		List<AclExperimentIdentity> experimentIdentity = aclRepository.findAll();
		assertNotNull("did not find entities", experimentIdentity);
		assertTrue("no entities found",experimentIdentity.size() > 0);
		AclExperimentIdentity identity = experimentIdentity.get(0);
		
		Experiment experiment = identity.getExperiment();
		assertNotNull("did not find attached TraitUom",experiment);
		assertNotNull("did not find attached Class",identity.getAclClass());
		assertTrue(identity.getAclClass() instanceof AclExperimentClass);
		assertNotNull("did not find attached Owner",identity.getOwner());
	}
	
	@Test
	public void testFindAclByExperiment() {
		Experiment experiment = experimentRepository.findOne(1L);
		AclExperimentIdentity acl = experiment.getAcl();
		assertNotNull("ACL Identities found",acl);
		Set<AclExperimentEntry> aces = acl.getEntries();
		assertNotNull("Ace Entries found",aces);
	}
}

