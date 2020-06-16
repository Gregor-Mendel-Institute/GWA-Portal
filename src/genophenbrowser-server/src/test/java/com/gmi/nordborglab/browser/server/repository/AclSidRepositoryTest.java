package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.06.13
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */
public class AclSidRepositoryTest extends BaseTest {

    @Resource
    protected AclSidRepository aclSidRepository;

    @Test
    public void testFindBySid() {
        AclSid sid = aclSidRepository.findBySid("ROLE_USER");
        assertNotNull(sid);
        assertEquals("ROLE_USER", sid.getSid());
    }

    @Test
    public void testFindAllBySids() {
        List<String> sids = Lists.newArrayList();
        sids.add("2");
        sids.add("ROLE_USER");
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(sids);
        assertNotNull("did not find entities", aclSids);
        assertEquals(2, aclSids.size());
        assertEquals("ROLE_USER", aclSids.get(0).getSid());
        assertEquals("2", aclSids.get(1).getSid());
    }


}

