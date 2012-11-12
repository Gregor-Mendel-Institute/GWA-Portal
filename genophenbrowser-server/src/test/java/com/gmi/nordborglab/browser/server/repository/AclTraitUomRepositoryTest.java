package com.gmi.nordborglab.browser.server.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;

import com.gmi.nordborglab.browser.server.domain.acl.AclTraitUomClass;
import com.gmi.nordborglab.browser.server.domain.acl.AclTraitUomEntry;
import com.gmi.nordborglab.browser.server.domain.acl.AclTraitUomIdentity;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;


public class AclTraitUomRepositoryTest extends BaseTest {

	@Resource
	protected AclTraitUomIdentityRepository aclRepository;
	
	@Resource 
	protected TraitUomRepository traitUomRepository;	
	
	@Test
	public void testFindAcls() {
		List<AclTraitUomIdentity> traitUomIdentities = aclRepository.findAll();
		assertNotNull("did not find entities", traitUomIdentities);
		assertTrue("no entities found",traitUomIdentities.size() > 0);
		AclTraitUomIdentity identity = traitUomIdentities.get(0);
		
		TraitUom traitUom = identity.getTraitUom();
		assertNotNull("did not find attached TraitUom",traitUom);
		assertNotNull("did not find attached Class",identity.getAclClass());
		assertTrue(identity.getAclClass() instanceof AclTraitUomClass);
		assertNotNull("did not find attached Owner",identity.getOwner());
	}
	
	@Test
	public void testFindAclByExperiment() {
		TraitUom traitUom = traitUomRepository.findOne(1L);
		AclTraitUomIdentity acl = traitUom.getAcl();
		assertNotNull("ACL Identities found",acl);
		Set<AclTraitUomEntry> aces = acl.getEntries();
		assertNotNull("Ace Entries found",aces);
	}
}
