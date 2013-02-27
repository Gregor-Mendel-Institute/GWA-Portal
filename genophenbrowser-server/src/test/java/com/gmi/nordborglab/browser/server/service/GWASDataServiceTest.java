package com.gmi.nordborglab.browser.server.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import com.google.common.collect.Lists;
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
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.junit.Assert.*;

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
		ImmutableMap<String,GWASData> map = gwasDataService.getGWASDataByStudyId(1L);
		assertNotNull("nothing returned",map);
		assertTrue("chromosome found",map.containsKey("chr1"));
		GWASData data = map.get("chr1");
		assertNotNull("Positions are null",data.getPositions());
		assertNotNull("pvalues are null",data.getPvalues());
		assertEquals("Chr is incorred","chr1", data.getChr());
		assertEquals("Size of positions is not equal to size of pvalues",data.getPositions().length,data.getPvalues().length);
	}

    @Test
    public void testDeleteGWASResult() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST", "TEST",adminAuthorities);
        GWASResult gwasResult = gwasResultRepository.findOne(855L);
        List<GWASResult> list = gwasDataService.delete(gwasResult);
        assertEquals(0,list.size());
        assertNull(gwasResultRepository.findOne(855L));
    }

    @Test(expected=AccessDeniedException.class)
    public void testDeleteGWASResultAccessedDenied() {
        SecurityUtils.setAnonymousUser();
        GWASResult gwasResult = gwasResultRepository.findOne(855L);
        List<GWASResult> list = gwasDataService.delete(gwasResult);
    }
	
	@Test(expected=AccessDeniedException.class)
	public void testGetGWASDataByStudyIdAccessDenied() {
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
		gwasDataService.getGWASDataByStudyId(1L);
	}


    @Test
    public void testGetGWASDataByViewerId() {
        Collection<? extends GrantedAuthority> adminAuthorities = ImmutableList.of(new SimpleGrantedAuthority("ROLE_ADMIN")).asList();
        SecurityUtils.makeActiveUser("TEST","TEST", adminAuthorities);
        ImmutableMap<String,GWASData> map = gwasDataService.getGWASDataByViewerId(750L);
        assertNotNull("nothing returned",map);
        assertTrue("chromosome found",map.containsKey("chr1"));
        GWASData data = map.get("chr1");
        assertNotNull("Positions are null",data.getPositions());
        assertNotNull("pvalues are null",data.getPvalues());
        assertEquals("Chr is incorred","chr1", data.getChr());
        assertEquals("Size of positions is not equal to size of pvalues",data.getPositions().length,data.getPvalues().length);
    }

    @Test(expected=AccessDeniedException.class)
    public void testGetGWASDataByViewerIdAccessDenied() {
        SecurityUtils.setAnonymousUser();
        gwasDataService.getGWASDataByViewerId(750L);
    }

    @Test(expected=AccessDeniedException.class)
    public void testGWASUploadDataAccessionDeniedForAnonymousUser() {
        SecurityUtils.setAnonymousUser();
        try {
            gwasDataService.uploadGWASResult(null);
        }
        catch (IOException e) {}
    }
}
