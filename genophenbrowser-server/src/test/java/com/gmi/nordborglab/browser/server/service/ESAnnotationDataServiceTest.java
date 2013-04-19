package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.acls.model.MutableAclService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/2/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ESAnnotationDataServiceTest extends BaseTest {

    @Resource(name = "ES")
    protected AnnotationDataService annotationDataService;

    @Before
    public void setUp() {


    }

    @After
    public void clearContext() {
    }

    @Test
    public void testGetSNPAnnotations() {
        int[] positions = {4900,5599,5731};
        List<SNPAnnot> annotations = annotationDataService.getSNPAnnotations("chr1",positions);
        assertNotNull(annotations);
        assertEquals(positions.length,annotations.size());
        assertEquals(annotations.get(0).getAnnotation(),"S");
        assertEquals(annotations.get(1).getAnnotation(),"NS");
        assertEquals(annotations.get(2).getAnnotation(),"*");
    }
}