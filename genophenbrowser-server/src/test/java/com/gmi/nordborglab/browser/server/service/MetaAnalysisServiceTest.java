package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.acls.model.MutableAclService;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.06.13
 * Time: 20:05
 * To change this template use File | Settings | File Templates.
 */
public class MetaAnalysisServiceTest extends BaseTest {

    @Resource
    private MetaAnalysisService service;

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
    public void findAllAnalysisForRegion() {
        SecurityUtils.setAnonymousUser();
        List<MetaSNPAnalysis> list = service.findAllAnalysisForRegion(9581605, 9604507, "2");
        assertNotNull(list);
        assertEquals(7, list.size());
        MetaSNPAnalysis analysis = list.get(0);
        assertNotNull(analysis.getSnpAnnotation());
    }


}
