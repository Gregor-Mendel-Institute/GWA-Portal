package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.Iterables;


public class TraitUomServiceTest extends BaseTest {
	
	@Resource
	private TraitUomService service;
	
	@Resource
	private TraitUomRepository repository;
	
	@Resource 
	private UserRepository userRepository;

	@Before
	public void setUp() {
	}
	
	@After
	public void clearContext() {
		SecurityUtils.clearContext();
	}
	
	
	@Test
	public void testFindPhenotypesByExperiment() {
		SecurityUtils.setAnonymousUser();
		TraitUomPage page = service.findPhenotypesByExperiment(1L, 0, 50);
		assertEquals(107L, page.getTotalElements());
		assertEquals(page.getNumberOfElements(), 50);
		assertEquals(50, page.getContent().size());
	}
	
	@Test
	public void testCountPhenotypesByExperiment() {
		SecurityUtils.setAnonymousUser();
		int count = service.countPhenotypeByExperimentCount(1L);
		assertEquals(107, count);
	}
	
	@Test
	public void checkNoVisiblePermissionWhenNoAdmin() {
		SecurityUtils.setAnonymousUser();
		TraitUom phenotype = service.findPhenotype(1L);
		assertTrue((phenotype.getUserPermission().getMask() & BasePermission.READ.getMask()) == BasePermission.READ.getMask()); 
		assertFalse(phenotype.isOwner());
	}
	
	@Test
	public void testFindPhenotype()  {
		SecurityUtils.setAnonymousUser();
		TraitUom phenotype = service.findPhenotype(1L);
		assertNotNull("couldn't find phenotype", phenotype);
		assertEquals("uncorrect number of obsUnits",new Long(167),phenotype.getNumberOfObsUnits());
		assertEquals("uncorrect number of studies",new Long(2),phenotype.getNumberOfStudies());
		assertNotNull("statisticTypes not loaded",phenotype.getStatisticTypes());
		assertEquals("correct amount of statisticTypes",2,phenotype.getStatisticTypes().size());
		assertEquals("wrong number of traits in statitictype",167,Iterables.get(phenotype.getStatisticTypes(), 0).getNumberOfTraits().intValue());
	}
	
	
	
	
	@Test
	public void checkVisiblePermissionsWhenAdmin() {
		createTestUser("ROLE_ADMIN");
		TraitUom phenotype = service.findPhenotype(1L);
		assertTrue(phenotype.isOwner());
		assertTrue((phenotype.getUserPermission().getMask() & BasePermission.WRITE.getMask()) == BasePermission.WRITE.getMask()); 
		assertTrue((phenotype.getUserPermission().getMask() & BasePermission.ADMINISTRATION.getMask()) == BasePermission.ADMINISTRATION.getMask());
	}
	
	
	
	
	@Test
	public void checkNoEditPermissionWhenNoPermission() {
		createTestUser("ROLE_USER");
		TraitUom phenotype = service.findPhenotype(1L);
		assertFalse((phenotype.getUserPermission().getMask() & BasePermission.WRITE.getMask()) == BasePermission.WRITE.getMask()); 
		assertFalse((phenotype.getUserPermission().getMask() & BasePermission.DELETE.getMask()) == BasePermission.DELETE.getMask());
	}
	
	
	

	@Test(expected=AccessDeniedException.class)
	public void checkPermissionToModifyExperiment() {
		createTestUser("ROLE_USER");
		TraitUom phenotype = repository.findOne(1L);
		phenotype.setLocalTraitName("test");
		service.save(phenotype);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void checkAnonymousNotAllowedToEdit() {
		SecurityUtils.setAnonymousUser();
		TraitUom phenotype = repository.findOne(1L);
		phenotype.setLocalTraitName("test");
		service.save(phenotype);
	}
	
	@Test(expected=RuntimeException.class)
	public void checkSaveNotForCreate() {
		createTestUser("ROLE_ADMIN");
		TraitUom phenotype = new TraitUom();
		phenotype.setLocalTraitName("test");
		service.save(phenotype);
	}
	
	@Test
	public void testFindPhenotypesByPassportId() {
		SecurityUtils.setAnonymousUser();
		List<TraitUom> traits = service.findPhenotypesByPassportId(1L);
		assertEquals(254, traits.size());
	}
	
	
	private void createTestUser(String role) {
		AppUser appUser = new AppUser("test@test.at");
		appUser.setOpenidUser(false);
		Md5PasswordEncoder encoder = new Md5PasswordEncoder();
		appUser.setPassword(encoder.encodePassword(SecurityUtils.TEST_PASSWORD, null));
		List<Authority> authorities = new ArrayList<Authority>();
		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
		SimpleGrantedAuthority auth = new SimpleGrantedAuthority(role); 
		grantedAuthorities.add(auth);
		Authority authority = new Authority();
		authority.setAuthority(role);
		authorities.add(authority);
		appUser.setAuthorities(authorities);
		userRepository.save(appUser);
		SecurityUtils.makeActiveUser(SecurityUtils.TEST_USERNAME, SecurityUtils.TEST_PASSWORD,grantedAuthorities);
	}
}
