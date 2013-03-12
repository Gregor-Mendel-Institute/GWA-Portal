package com.gmi.nordborglab.browser.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadValue;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
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
	public void testFindPhenotypesByExperiment() {
		SecurityUtils.setAnonymousUser();
		TraitUomPage page = service.findPhenotypesByExperiment(1L, 0, 50);
		assertEquals(107L, page.getTotalElements());
		assertEquals(page.getNumberOfElements(), 50);
		assertEquals(50, page.getContent().size());
		assertNotNull(page.getContent().get(0).getTraitOntologyTerm());
		assertEquals("TO:0000344", page.getContent().get(0).getTraitOntologyTerm().getAcc());
		assertNotNull(page.getContent().get(0).getTraitOntologyTerm().getChilds());
	}

    @Test
    public void testFindPhenotypesByExperimentAndAclWithNoPermission() {
        SecurityUtils.setAnonymousUser();
        List<TraitUom> traits = service.findPhenotypesByExperimentAndAcl(1L, CustomPermission.EDIT.getMask());
        assertEquals(0L, traits.size());
    }

    @Test
    public void testFindPhenotypesByExperimentAndAcl() {
        createTestUser("ROLE_ADMIN");
        List<TraitUom> traits = service.findPhenotypesByExperimentAndAcl(1L, CustomPermission.EDIT.getMask());
        assertEquals(107L, traits.size());
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
		assertTrue((phenotype.getUserPermission().getMask() & CustomPermission.READ.getMask()) == CustomPermission.READ.getMask());
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
		assertTrue((phenotype.getUserPermission().getMask() & CustomPermission.EDIT.getMask()) == CustomPermission.EDIT.getMask());
		assertTrue((phenotype.getUserPermission().getMask() & CustomPermission.ADMINISTRATION.getMask()) == CustomPermission.ADMINISTRATION.getMask());
	}
	
	
	
	
	@Test
	public void checkNoEditPermissionWhenNoPermission() {
		createTestUser("ROLE_USER");
		TraitUom phenotype = service.findPhenotype(1L);
		assertFalse((phenotype.getUserPermission().getMask() & CustomPermission.EDIT.getMask()) == CustomPermission.EDIT.getMask());
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
	
	@Test()
	public void testSave() {
		createTestUser("ROLE_ADMIN");
		TraitUom phenotype = repository.findOne(1L);
		phenotype.setLocalTraitName("test");
		phenotype.setToAccession("TO:0006046");
		TraitUom saved = service.save(phenotype);
		assertNotNull(saved);
		assertEquals("test",saved.getLocalTraitName());
		assertEquals("TO:0006046",saved.getToAccession());
		assertNotNull(saved.getTraitOntologyTerm());
		assertEquals("TO:0006046",saved.getTraitOntologyTerm().getAcc());
		
		
	}
	
	@Test
	public void testFindPhenotypesByPassportId() {
		SecurityUtils.setAnonymousUser();
		List<TraitUom> traits = service.findPhenotypesByPassportId(1L);
		assertEquals(254, traits.size());
	}
	
	@Test
	public void testFindAllPhenotypes() {
		SecurityUtils.setAnonymousUser();
		TraitUomPage traitPage = service.findAll(null,null,null,null,0,50);
		assertEquals(50,traitPage.getNumberOfElements());
		assertEquals(0,traitPage.getNumber());
		assertEquals(600, traitPage.getTotalElements());
	}

    @Test(expected = AccessDeniedException.class)
    public void testSavePhenotypeUploadNoAnnonymousAllowed() {
        SecurityUtils.setAnonymousUser();
        service.savePhenotypeUploadData(1L,new PhenotypeUploadData());
    }

    @Test(expected = NotFoundException.class)
    public void testSavePhenotypeUploadNoPermissionFound() {
        createTestUser("ROLE_USER");
        service.savePhenotypeUploadData(1L,new PhenotypeUploadData());
    }

    @Test
    public void testSavePhenotypeUploadInExistingExperiment() {
        createTestUser("ROLE_ADMIN");
        PhenotypeUploadData data = new PhenotypeUploadData();
        getPhenotypeUploadData(data);
        Long id = service.savePhenotypeUploadData(1L,data);
        TraitUom traitUom = repository.findOne(id);
        assertPhenotypeUploadData(data, traitUom);
        assertEquals(1,traitUom.getExperiment().getId().longValue());
    }

    @Test
    public void testSavePhenotypeUploadInEmptyExperiment() {
        createTestUser("ROLE_ADMIN");
        PhenotypeUploadData data = new PhenotypeUploadData();
        getPhenotypeUploadData(data);
        Long id = service.savePhenotypeUploadData(5451L,data);
        TraitUom traitUom = repository.findOne(id);
        assertPhenotypeUploadData(data, traitUom);
        assertEquals(5451L,traitUom.getExperiment().getId().longValue());
    }

    @Test
    public void testSavePhenotypeUploadAndPermission() {
        createTestUser("ROLE_USER");
        PhenotypeUploadData data = new PhenotypeUploadData();
        getPhenotypeUploadData(data);
        Long id = service.savePhenotypeUploadData(5600L,data);
        TraitUom traitUom = repository.findOne(id);
        Acl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,id);
        List<Sid> sids = new ArrayList<Sid>();
        sids.add(new PrincipalSid(SecurityUtils.TEST_USERNAME));
        acl = aclService.readAclById(oid, sids);
        assertPermission(acl,sids);
        List<Sid> adminSids = Arrays.asList((Sid)new GrantedAuthoritySid("ROLE_ADMIN"));
        acl = aclService.readAclById(oid, adminSids);
        assertPermission(acl,sids);
        traitUom.setLocalTraitName("modified234");
        TraitUom modifiedTraitUom = service.save(traitUom);
        assertNotNull(traitUom);
        assertEquals("modified234", modifiedTraitUom.getLocalTraitName());
    }

    private void getPhenotypeUploadData(PhenotypeUploadData data) {
        data.setName("Testphenotype");
        data.setTraitOntology("TO:TEST");
        data.setEnvironmentOntology("TO:TEST");
        data.setProtocol("TEST");
        data.setUnitOfMeasure("days");
        data.setValueHeader(Arrays.asList("mean", "std"));

        List<PhenotypeUploadValue> values = new ArrayList<PhenotypeUploadValue>();
        PhenotypeUploadValue value = null;
        value = new PhenotypeUploadValue();
        value.setPassportId(1L);
        value.setValues(Arrays.asList("1", "2"));
        values.add(value);
        value = new PhenotypeUploadValue();
        value.setPassportId(6959L);
        value.setValues(Arrays.asList("3", "4"));
        values.add(value);
        data.setPhenotypeUploadValues(values);
    }

    private void assertPhenotypeUploadData(PhenotypeUploadData data, TraitUom traitUom) {
        assertEquals(data.getName(),traitUom.getLocalTraitName());
        assertEquals(data.getTraitOntology(),traitUom.getToAccession());
        assertEquals(data.getEnvironmentOntology(),traitUom.getEoAccession());
        assertEquals(data.getProtocol(),traitUom.getTraitProtocol());
        assertNotNull(traitUom.getTraits());
        assertEquals(4,traitUom.getTraits().size());
        assertNotNull(traitUom.getExperiment());
        for (PhenotypeUploadValue value : data.getPhenotypeUploadValues()) {
            for (int i=0;i<value.getValues().size();i++) {
                String val = value.getValues().get(0);
                boolean found = false;
                for (Trait trait:traitUom.getTraits()) {
                    if (trait.getValue().equals(val))  {
                        assertEquals(data.getValueHeader().get(i),trait.getStatisticType().getStatType());
                        assertEquals(value.getPassportId(),trait.getObsUnit().getStock().getPassport().getId());
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
                break;
            }
        }
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

    private void assertPermission(Acl acl,List<Sid> sids) {
        assertTrue(acl.isGranted(Arrays.asList(CustomPermission.ADMINISTRATION), sids, false));
        assertTrue(acl.isGranted(Arrays.asList(CustomPermission.EDIT), sids, false));
        assertTrue(acl.isGranted(Arrays.asList(CustomPermission.READ), sids, false));
    }
}
