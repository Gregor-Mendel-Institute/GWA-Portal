package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;


public class ExperimentServiceTest extends BaseTest {
	
	@Resource
	private ExperimentService service;
	
	@Resource 
	private UserRepository userRepository;
	
	@Resource 
	private ExperimentRepository repository;
	
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
	public void findExperimentCheckPermission() {
		SecurityUtils.setAnonymousUser();
		Experiment exp = service.findExperiment(3L);
		assertNotNull(exp);
	}
	
	@Ignore
	@Test(expected=AccessDeniedException.class)
	public void findExperimentAndThrowSecurityException() {
		SecurityUtils.setAnonymousUser();
		Experiment exp = service.findExperiment(2L);
	}
	
	@Test
	public void getAllExperimentsWhenAdmin() {
		createTestUser("ROLE_ADMIN");
		long count = repository.count();
		long foundCount= service.findByAcl(0,(int)count).getTotalElements();
		assertEquals(count,foundCount);
	}
	
	@Test
	public void getPagedExperimentsWhenAdmin() {
		createTestUser("ROLE_ADMIN");
		long count = repository.count();
		ExperimentPage page = service.findByAcl(1,5);
		assertNotNull(page);
		assertEquals(5, page.getContent().size());
		assertEquals(count, page.getTotalElements());
	}
	
	@Test
	public void createExperimentAndCheckPermissions() {
		createTestUser("ROLE_USER");
		Experiment experiment = new Experiment();
		experiment.setName("test");
		experiment.setDesign("test");
		experiment.setOriginator("test");
		experiment.setComments("test");
		Experiment savedExperiment = service.save(experiment);
		assertNotNull(savedExperiment);
		assertNotNull(savedExperiment.getId());
		assertEquals(experiment.getName(), savedExperiment.getName());
		Acl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class,savedExperiment.getId());
		List<Sid> sids = new ArrayList<Sid>();
		sids.add(new PrincipalSid(SecurityUtils.TEST_USERNAME));
		acl = aclService.readAclById(oid, sids);
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(BasePermission.ADMINISTRATION);
		assertTrue(acl.isGranted(permissions, sids, false));
		
		savedExperiment.setName("modified");
		Experiment modifiedExperiment = service.save(savedExperiment);
		assertNotNull(savedExperiment);
		assertEquals("modified", modifiedExperiment.getName());
	}
	
	
	@Test
	public void checkNoVisiblePermissionWhenNoAdmin() {
		createTestUser("ROLE_ANONYMOUS");
		Experiment experiment = service.findExperiment(1L);
		assertTrue((experiment.getUserPermission().getMask() & BasePermission.READ.getMask()) == BasePermission.READ.getMask()); 
		assertFalse(experiment.isOwner());
	}
	
	
	@Test
	public void checkVisiblePermissionsWhenAdmin() {
		createTestUser("ROLE_ADMIN");
		Experiment experiment = service.findExperiment(1L);
		assertTrue((experiment.getUserPermission().getMask() & BasePermission.WRITE.getMask()) == BasePermission.WRITE.getMask()); 
		assertTrue((experiment.getUserPermission().getMask() & BasePermission.ADMINISTRATION.getMask()) == BasePermission.ADMINISTRATION.getMask());
	}
	
	
	@Test
	public void checkNoEditPermissionWhenNoPermission() {
		createTestUser("ROLE_USER");
		Experiment experiment = service.findExperiment(1L);
		assertFalse((experiment.getUserPermission().getMask() & BasePermission.WRITE.getMask()) == BasePermission.WRITE.getMask()); 
		assertFalse((experiment.getUserPermission().getMask() & BasePermission.DELETE.getMask()) == BasePermission.DELETE.getMask());
	}
	
	
	

	@Test(expected=AccessDeniedException.class)
	public void checkPermissionToModifyExperiment() {
		createTestUser("ROLE_USER");
		Experiment experiment = repository.findOne(1L);
		experiment.setName("test");
		service.save(experiment);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void checkAnonymousNotAllowedToSave() {
		SecurityUtils.setAnonymousUser();
		Experiment experiment = new Experiment();
		experiment.setName("test");
		service.save(experiment);
	}
	
	@Test(expected=AccessDeniedException.class)
	public void checkAnonymousNotAllowedToEdit() {
		SecurityUtils.setAnonymousUser();
		Experiment experiment = repository.findOne(1L);
		experiment.setName("test");
		service.save(experiment);
	}
	
	@Test(expected=AccessDeniedException.class)
	@Ignore
	public void checkAnonymousNotAllowedToDelete() {
		
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
