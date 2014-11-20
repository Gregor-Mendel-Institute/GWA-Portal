package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAlleleInfo;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

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
        int[] positions = {4900, 5599, 5731};
        List<SNPAnnot> annotations = annotationDataService.getSNPAnnotations("chr1", positions);
        assertNotNull(annotations);
        assertEquals(positions.length, annotations.size());
        assertEquals(annotations.get(0).getAnnotation(), "S");
        assertEquals(annotations.get(1).getAnnotation(), "NS");
        assertEquals(annotations.get(2).getAnnotation(), "*");
    }

    @Test
    public void testGetGeneById() {
        Gene gene = annotationDataService.getGeneById("AT2G01028");
        assertNotNull(gene);
        assertEquals("AT2G01028", gene.getName());
        assertEquals(23971, gene.getStart());
        assertEquals(26923, gene.getEnd());
        assertEquals(0, gene.getStrand());
    }

    @Test
    public void testGetSNPAlleleInfoForAllPassports() {
        SNPAlleleInfo info = annotationDataService.getSNPAlleleInfo(1L, 1, 4880, null);
        assertThat("null returned", info, Is.is(notNullValue()));
        assertThat("wrong number of elements", info.getAlleles().size(), is(1386));
        checkSNPInfo(info.getSnpAnnot());
    }

    @Test
    public void testGetSNPAlleleInfo() {
        List<Long> passportIds = Lists.asList(new Long(7000), new Long(8320L), null);
        SNPAlleleInfo info = annotationDataService.getSNPAlleleInfo(1L, 1, 4880, passportIds);
        assertThat("null returned", info, Is.is(notNullValue()));
        assertThat("wrong number of elements", info.getAlleles().size(), is(2));
        checkSNPInfo(info.getSnpAnnot());
    }

    private void checkSNPInfo(SNPAnnot snpAnnot) {
        assertThat(snpAnnot.getAnnotation(), is("S"));
        assertThat(snpAnnot.getRef(), is("C"));
        assertThat(snpAnnot.getAlt(), is("T"));
    }

}