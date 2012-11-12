package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.PermissionPrincipal;
import com.gmi.nordborglab.browser.server.domain.acl.SearchPermissionUserRole;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.CustomAcl;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;

public class PermissionServiceTest extends BaseTest {
	
	@Resource 
	protected ExperimentRepository experimentReposity;
	
	
	
	@Resource
	protected PermissionService permissionService;
	
	
	@Before
	public void setUp() {
		
		
	}
	
	@After
	public void clearContext() {
		SecurityUtils.clearContext();
	}
	
	
	@Test()
	public void testGetPermissionsNotAllowedWhenNoAdministrationPermission() {
		SecurityUtils.setAnonymousUser();
		Experiment experiment = experimentReposity.findOne(1L);
		try {
			permissionService.getPermissions(experiment);
			fail("No exception thrown");
		}
		catch (Exception e) {
			assert(e instanceof AccessDeniedException || e instanceof NotFoundException);
		}
	}
	
	@Test
	public void testUpdatePermissionNotAllowedWhenNoAdministrationPermission() {
		SecurityUtils.setAnonymousUser();
		Experiment experiment = experimentReposity.findOne(1L);
		List<CustomAccessControlEntry> aces = new ArrayList<CustomAccessControlEntry>();
		aces.add(new CustomAccessControlEntry(1L,1, true));
		CustomAcl acl = new CustomAcl(aces, true);
		try {
			permissionService.updatePermissions(experiment,acl);
			fail("No exception thrown");
		}
		catch (Exception e) {
			assert(e instanceof AccessDeniedException || e instanceof NotFoundException);
		}
	}
	
	@Test
	public void testGetPermissions() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
	    Experiment experiment = experimentReposity.findOne(1L);
	    CustomAcl acl = permissionService.getPermissions(experiment);
		assertNotNull("no ace entries found",acl);
		assertNotNull(acl.getEntries());
	}
	
	@Test
	public void testNoUpdateWhenUnknownRoleAndUser() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
	    Experiment experiment = experimentReposity.findOne(1L);
	    CustomAcl acl = permissionService.getPermissions(experiment);
	    int count  = acl.getEntries().size();
	    CustomAccessControlEntry newAce = new CustomAccessControlEntry(null, 1, true);
	    newAce.setPrincipal(new PermissionPrincipal("ROLE_UNKNOW","ROLE_ANONYMOUS",false));
	    acl.getEntries().add(newAce);
	    newAce = new CustomAccessControlEntry(null, 1, true);
	    newAce.setPrincipal(new PermissionPrincipal("UNKNOWNUSER","Fernando Rabanal",true));
	    acl.getEntries().add(newAce);
	    CustomAcl newAcl = permissionService.updatePermissions(experiment, acl);
	    assertEquals("acl for unknown role/user was created", count,newAcl.getEntries().size());
	}
	
	@Test
	public void testNoDuplicatePermissionSaved() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
		PermissionPrincipal principal = null;
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
	    Experiment experiment = experimentReposity.findOne(1L);
	    CustomAcl acl = permissionService.getPermissions(experiment);
	    int count  = acl.getEntries().size();
	    CustomAccessControlEntry newAce = new CustomAccessControlEntry(null, 1, true);
	    //newAce.set("ROLE_ANONYMOUS");
	    newAce.setPrincipal(new PermissionPrincipal("ROLE_ANONYMOUS","ROLE_ANONYMOUS",false));
	    acl.getEntries().add(newAce);
	    newAce = new CustomAccessControlEntry(null, 1, true);
	    newAce.setPrincipal(new PermissionPrincipal("fer.rabanal@gmail.com","Fernando Rabanal",true));
	    acl.getEntries().add(newAce);
	    acl.getEntries().add(newAce);
	    CustomAcl newAcl = permissionService.updatePermissions(experiment, acl);
	    assertEquals("acl for unknown role was created", count+1,newAcl.getEntries().size());
	}
	
	@Test
	public void testUpdatePermissions() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
	    Experiment experiment = experimentReposity.findOne(1L);
	    CustomAcl acl = permissionService.getPermissions(experiment);
	    int count = acl.getEntries().size();
	    CustomAccessControlEntry ace = acl.getEntries().get(0);
	    ace.setMask(2);
	    ace = acl.getEntries().get(1);
	    ace.setMask(0);
	    acl.setIsEntriesInheriting(true);
	    CustomAccessControlEntry newAce = new CustomAccessControlEntry(null, 1, true);
	    newAce.setPrincipal(new PermissionPrincipal("ROLE_ANONYMOUS","ROLE_ANONYMOUS",false));
	    acl.getEntries().add(newAce);
	    newAce = new CustomAccessControlEntry(null,1,true);
	    newAce.setPrincipal(new PermissionPrincipal("fer.rabanal@gmail.com","Fernando Rabanal",true));
	    acl.getEntries().add(newAce);
	    CustomAcl newAcl = permissionService.updatePermissions(experiment, acl);
	    assertNotSame(newAcl, ace);
	    assertTrue("Inheriting wasn't set", newAcl.getIsEntriesInheriting());
	    assertEquals("mask was not properly changed", 2,newAcl.getEntries().get(0).getMask());
	    assertEquals("Ace count not correct", count+1,newAcl.getEntries().size());
	    CustomAccessControlEntry grantedAce = newAcl.getEntries().get(newAcl.getEntries().size()-2);
	    assertEquals("Mask of granted authority wrong",1, grantedAce.getMask());
	    assertEquals("Wrong granted authority", "ROLE_ANONYMOUS",grantedAce.getPrincipal().getId());
	    CustomAccessControlEntry userAce = newAcl.getEntries().get(newAcl.getEntries().size()-1);
	    assertEquals("Wrong user", "fer.rabanal@gmail.com",userAce.getPrincipal().getId());
	    assertEquals("Mask of user permission wrong",1, userAce.getMask());
	}
	
	@Test
	public void testSearchForUsers() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
	    SearchPermissionUserRole searchResult = permissionService.searchUserAndRoles("Fer");
	    assertEquals(1, searchResult.getPrincipals().size());
	    assertEquals("Fernando Rabanal", searchResult.getPrincipals().get(0).getName());
	}
	
	@Test
	public void testSearchForRole() {
		Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
	    SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
	    SearchPermissionUserRole searchResult = permissionService.searchUserAndRoles("ROLE");
	    assertEquals(3, searchResult.getPrincipals().size());
	    assertEquals("ROLE_ADMIN", searchResult.getPrincipals().get(0).getId());
	}
	
	
}
