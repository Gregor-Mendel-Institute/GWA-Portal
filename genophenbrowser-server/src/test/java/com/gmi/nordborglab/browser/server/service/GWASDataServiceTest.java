package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.SNPGWASInfo;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;
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

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GWASDataServiceTest extends BaseTest {

    @Resource
    private GWASDataService gwasDataService;

    @Resource
    private GWASResultRepository gwasResultRepository;


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
    public void testGetGWASDataByStudyId() {
        SecurityUtils.setAnonymousUser();
        GWASData gwasdata = gwasDataService.getGWASDataByStudyId(1L);
        assertNotNull(gwasdata);
        Map<String, ChrGWAData> map = gwasdata.getChrGWASData();
        assertNotNull("nothing returned", map);
        assertTrue("chromosome found", map.containsKey("chr1"));
        ChrGWAData data = map.get("chr1");
        assertNotNull("Positions are null", data.getPositions());
        assertNotNull("pvalues are null", data.getPvalues());
        assertEquals("Chr is incorred", "chr1", data.getChr());
        assertEquals("Size of positions is not equal to size of pvalues", data.getPositions().length, data.getPvalues().length);
    }

    @Test
    public void testGetSNPGWASInfoByStudyId() {
        SecurityUtils.setAnonymousUser();
        SNPGWASInfo info = gwasDataService.getSNPGWASInfoByStudyId(156L, 2, 9581605);
        assertNotNull(info);
        assertThat(info.getPosition(), is(9581605));
        assertThat(info.getChr(), is("Chr2"));
        assertThat(info.getScore(), is(9.917914390563965));
        assertThat(info.getNumberOfSNPs(), is(206087L));
        assertThat(info.getBonferroniScore(), is(6.615080592943772));
        assertThat(info.getMac(), is(47));
        assertThat(info.getMaf(), is(0.28143712878227234));
    }


    @Test
    public void testGetGWASDataByViewerId() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        GWASData gwasData = gwasDataService.getGWASDataByViewerId(400L);
        Map<String, ChrGWAData> map = gwasData.getChrGWASData();
        assertNotNull("nothing returned", map);
        assertTrue("chromosome found", map.containsKey("Chr1"));
        ChrGWAData data = map.get("Chr1");
        assertNotNull("Positions are null", data.getPositions());
        assertNotNull("pvalues are null", data.getPvalues());
        assertEquals("Chr is incorred", "Chr1", data.getChr());
        assertEquals("Size of positions is not equal to size of pvalues", data.getPositions().length, data.getPvalues().length);
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetGWASDataByViewerIdAccessDenied() {
        SecurityUtils.setAnonymousUser();
        gwasDataService.getGWASDataByViewerId(750L);
    }

    @Test(expected = AccessDeniedException.class)
    public void testGWASUploadDataAccessionDeniedForAnonymousUser() {
        SecurityUtils.setAnonymousUser();
        try {
            gwasDataService.uploadGWASResult(null);
        } catch (IOException e) {
        }
    }

    @Test
    public void testFindOneGWASResult() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        GWASResult result = gwasDataService.findOneGWASResult(850L);
        assertNotNull(result);
        assertEquals(850L, result.getId().longValue());

    }

    @Test(expected = AccessDeniedException.class)
    public void testFindOneGWASResultAccessDenied() {
        SecurityUtils.setAnonymousUser();
        GWASResult result = gwasDataService.findOneGWASResult(850L);
    }

    @Test(expected = AccessDeniedException.class)
    public void testSaveAccessDeniedException() {
        SecurityUtils.setAnonymousUser();
        GWASResult actual = gwasDataService.findOneGWASResult(901L);
        gwasDataService.save(actual);
    }

    @Test()
    public void testSave() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        GWASResult actual = gwasDataService.findOneGWASResult(850L);
        actual.setName("TEST");
        GWASResult updated = gwasDataService.save(actual);
        assertNotNull(updated);
        assertEquals("TEST", updated.getName());
    }


    @Test(expected = AccessDeniedException.class)
    public void testDeleteGWASResultAccessedDenied() {
        SecurityUtils.setAnonymousUser();
        GWASResult gwasResult = gwasResultRepository.findOne(855L);
        List<GWASResult> list = gwasDataService.delete(gwasResult);
    }

    @Test
    public void testDeleteGWASResult() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        GWASResult gwasResult = gwasResultRepository.findOne(850L);
        List<GWASResult> list = gwasDataService.delete(gwasResult);
        assertEquals(0, list.size());
        assertNull(gwasResultRepository.findOne(850L));
    }

    @Test(expected = AccessDeniedException.class)
    public void testGetGWASDataByStudyIdAccessDenied() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST", adminAuthorities);
        ObjectIdentity oid = new ObjectIdentityImpl(Experiment.class, 1L);
        List<Sid> authorities = Collections.singletonList((Sid) new GrantedAuthoritySid("ROLE_ANONYMOUS"));
        MutableAcl acl = (MutableAcl) aclService.readAclById(oid, authorities);

        for (int i = 0; i < acl.getEntries().size(); i++) {
            if (acl.getEntries().get(i).getSid().equals(authorities.get(0))) {
                acl.deleteAce(i);
                break;
            }
        }
        aclService.updateAcl(acl);
        SecurityUtils.setAnonymousUser();
        gwasDataService.getGWASDataByStudyId(1L);
    }
}
