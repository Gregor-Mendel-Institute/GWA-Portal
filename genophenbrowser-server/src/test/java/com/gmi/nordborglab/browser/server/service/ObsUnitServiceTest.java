package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.ObsUnitPage;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;

public class ObsUnitServiceTest extends BaseTest {

	@Resource
	private ObsUnitService service;


	@Resource
	private MutableAclService aclService;

	@Before
	public void setUp() {
	}

	@After
	public void clearContext() {
		SecurityUtils.clearContext();
	}

	@Test
	public void testFindObsUnitsByPhenotypeId() {
		SecurityUtils.setAnonymousUser();
		ObsUnitPage page = service.findObsUnits(1L, 0, 50);
		assertEquals(167, page.getTotalElements());
		assertEquals(50, page.getNumberOfElements());
		assertEquals(50, page.getContent().size());
	}
	
	@Test
	public void testFindObsUnitsWithNoGenotype() {
		SecurityUtils.setAnonymousUser();
		List<ObsUnit> obsUnits = service.findObsUnitWithNoGenotype(1L, 1L);
		assertNotNull("nothin returned",obsUnits);
		assertEquals("Wrong number returned",0, obsUnits.size());
	}

	@Test(expected=AccessDeniedException.class)
	public void testFindObsUnitsByPhenotypeIdAccessedDenied() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
		ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class,1L);
		List<Sid> authorities = Collections.singletonList((Sid)new GrantedAuthoritySid("ROLE_ANONYMOUS"));
		MutableAcl acl = (MutableAcl)aclService.readAclById(oid, authorities);
		
		for (int i=0;i<acl.getEntries().size();i++) {
			if (acl.getEntries().get(i).getSid().equals(authorities.get(0)))
			{
				acl.deleteAce(i);
				break;
			}
		}
		aclService.updateAcl(acl);
		SecurityUtils.setAnonymousUser();
		ObsUnitPage page = service.findObsUnits(1L, 0, 50);
	}
	

	@Test(expected=AccessDeniedException.class)
	public void testFindObsUnitsWithNoGenotypeAccessedDenied() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
		ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class,1L);
		List<Sid> authorities = Collections.singletonList((Sid)new GrantedAuthoritySid("ROLE_ANONYMOUS"));
		MutableAcl acl = (MutableAcl)aclService.readAclById(oid, authorities);
		
		for (int i=0;i<acl.getEntries().size();i++) {
			if (acl.getEntries().get(i).getSid().equals(authorities.get(0)))
			{
				acl.deleteAce(i);
				break;
			}
		}
		aclService.updateAcl(acl);
		SecurityUtils.setAnonymousUser();
		List<ObsUnit> obsUnits = service.findObsUnitWithNoGenotype(1L, 1L);
	}
}