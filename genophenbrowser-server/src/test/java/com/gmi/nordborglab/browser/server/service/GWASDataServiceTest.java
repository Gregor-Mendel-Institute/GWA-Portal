package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ObsUnitPage;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class GWASDataServiceTest extends BaseTest {

	@Resource
	private GWASDataService gwasDataService;


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
	public void testGetGWASData() {
		SecurityUtils.setAnonymousUser();
		ImmutableMap<String,GWASData> map = gwasDataService.getGWASData(1L);
		assertNotNull("nothing returned",map);
		assertTrue("chromosome found",map.containsKey("chr1"));
		GWASData data = map.get("chr1");
		assertNotNull("Positions are null",data.getPositions());
		assertNotNull("pvalues are null",data.getPvalues());
		assertEquals("Chr is incorred","chr1", data.getChr());
		assertEquals("Size of positions is not equal to size of pvalues",data.getPositions().length,data.getPvalues().length);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void testGetGWASDataAccessDenied() {
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
		gwasDataService.getGWASData(1L);
	}
}
