package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class TraitUomServiceTest extends BaseTest {

    @Resource
    private TraitUomService service;

    @Resource
    private TraitUomRepository repository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private MutableAclService aclService;

    @Resource
    private ExperimentRepository experimentRepository;


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
    public void testFindPhenotype() {
        SecurityUtils.setAnonymousUser();
        TraitUom phenotype = service.findPhenotype(1L);
        assertNotNull("couldn't find phenotype", phenotype);
        assertEquals("uncorrect number of obsUnits", new Long(167), phenotype.getNumberOfObsUnits());
        assertEquals("uncorrect number of studies", new Long(2), phenotype.getNumberOfStudies());
        assertNotNull("statisticTypes not loaded", phenotype.getStatisticTypes());
        assertEquals("correct amount of statisticTypes", 2, phenotype.getStatisticTypes().size());
        assertEquals("wrong number of traits in statitictype", 167, Iterables.get(phenotype.getStatisticTypes(), 0).getNumberOfTraits().intValue());
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


    @Test(expected = AccessDeniedException.class)
    public void checkPermissionToModifyExperiment() {
        createTestUser("ROLE_USER");
        TraitUom phenotype = repository.findOne(1L);
        phenotype.setLocalTraitName("test");
        service.save(phenotype);
    }

    @Test(expected = AccessDeniedException.class)
    public void checkAnonymousNotAllowedToEdit() {
        SecurityUtils.setAnonymousUser();
        TraitUom phenotype = repository.findOne(1L);
        phenotype.setLocalTraitName("test");
        service.save(phenotype);
    }

    @Test(expected = RuntimeException.class)
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
        assertEquals("test", saved.getLocalTraitName());
        assertEquals("TO:0006046", saved.getToAccession());
        assertNotNull(saved.getTraitOntologyTerm());
        assertEquals("TO:0006046", saved.getTraitOntologyTerm().getAcc());


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
        TraitUomPage traitPage = service.findAll(null, null, 0, 50);
        assertEquals(50, traitPage.getNumberOfElements());
        assertEquals(0, traitPage.getNumber());
        assertEquals(600, traitPage.getTotalElements());
    }

    @Test(expected = AccessDeniedException.class)
    public void testSavePhenotypeUploadNoAnnonymousAllowed() {
        SecurityUtils.setAnonymousUser();
        service.savePhenotypeUploadData(experimentRepository.findOne(1L), null, null);
    }

    @Test(expected = NotFoundException.class)
    public void testSavePhenotypeUploadNoPermissionFound() {
        createTestUser("ROLE_USER");
        service.savePhenotypeUploadData(experimentRepository.findOne(1L), null, null);
    }

    @Test
    public void testSavePhenotypeUpload() {
        createTestUser("ROLE_ADMIN");
        ExperimentUploadData data = getExperimentUploadData();
        List<TraitUom> result = service.savePhenotypeUploadData(data.getExperiment(), data.getPhenotypes(), data.getSampleData());
        assertPhenotypeUploadData(data, result);
    }


    private ExperimentUploadData getExperimentUploadData() {
        ExperimentUploadData data = new ExperimentUploadData();
        data.setExperiment(experimentRepository.findOne(1L));
        List<SampleData> sampleData = new ArrayList<SampleData>();
        List<String> values = Lists.newArrayList();
        List<PhenotypeUploadData> phenotypes = Lists.newArrayList();
        PhenotypeUploadData phenotypeUploadData = new PhenotypeUploadData();

        TraitUom traitUom = new TraitUom();
        traitUom.setLocalTraitName("Phenotype1");
        phenotypeUploadData.setTraitUom(traitUom);
        phenotypes.add(phenotypeUploadData);

        traitUom = new TraitUom();
        traitUom.setLocalTraitName("Phenotype2");
        phenotypeUploadData = new PhenotypeUploadData();
        phenotypeUploadData.setTraitUom(traitUom);
        phenotypes.add(phenotypeUploadData);


        phenotypeUploadData.setTraitUom(new TraitUom());
        SampleData value = null;
        value = new SampleData("1");
        value.setIdKnown(true);
        value.setPassportId(1L);
        value.addValue("", false);
        value.addValue("1", false);
        sampleData.add(value);

        value = new SampleData("1");
        value.setIdKnown(true);
        value.setPassportId(1L);
        value.addValue("1", false);
        value.addValue("", false);
        sampleData.add(value);

        value = new SampleData("6959");
        value.setIdKnown(true);
        value.setPassportId(6959L);
        value.addValue("1", false);
        value.addValue("1", false);
        sampleData.add(value);

        value = new SampleData("6959");
        value.setIdKnown(true);
        value.setPassportId(6959L);
        value.addValue("1", false);
        value.addValue("1", false);
        sampleData.add(value);

        data.setSampleData(sampleData);
        data.setPhenotypes(phenotypes);
        return data;
    }

    private void assertPhenotypeUploadData(ExperimentUploadData data, List<TraitUom> result) {
        assertThat(data.getPhenotypes().size(), is(result.size()));

        for (int i = 0; i < data.getPhenotypes().size(); i++) {
            TraitUom traitUom = result.get(i);
            TraitUom phenotype = data.getPhenotypes().get(i).getTraitUom();
            assertThat(traitUom.getLocalTraitName(), is(phenotype.getLocalTraitName()));
            assertThat(traitUom.getTraits(), notNullValue());
            assertThat(traitUom.getExperiment(), is(traitUom.getExperiment()));
            int j = 0;
            for (SampleData sample : data.getSampleData()) {
                String value = sample.getValues().get(i);
                if (value == null || value.isEmpty())
                    continue;
                boolean found = false;
                for (Trait trait : traitUom.getTraits()) {
                    if (trait.getObsUnit().getStock().getPassport().getId().equals(sample.getPassportId()) &&
                            trait.getValue().equals(value)) {
                        assertThat(sample.getPassportId(), is(trait.getObsUnit().getStock().getPassport().getId()));
                        assertThat(sample.getValues().get(i), is(trait.getValue()));
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
        appUser.setFirstname("Test");
        appUser.setLastname("test2");
        appUser.setOpenidUser(false);
        appUser.setPassword(SecurityUtils.TEST_PASSWORD);
        List<Authority> authorities = new ArrayList<Authority>();
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority(role);
        grantedAuthorities.add(auth);
        Authority authority = new Authority();
        authority.setAuthority(role);
        authorities.add(authority);
        appUser.setAuthorities(authorities);
        userRepository.save(appUser);
        SecurityUtils.makeActiveUser(SecurityUtils.TEST_USERNAME, SecurityUtils.TEST_PASSWORD, grantedAuthorities);
    }

    private void assertPermission(Acl acl, List<Sid> sids) {
        assertTrue(acl.isGranted(Arrays.asList(CustomPermission.ADMINISTRATION), sids, false));
        assertTrue(acl.isGranted(Arrays.asList(CustomPermission.EDIT), sids, false));
        assertTrue(acl.isGranted(Arrays.asList(CustomPermission.READ), sids, false));
    }
}
