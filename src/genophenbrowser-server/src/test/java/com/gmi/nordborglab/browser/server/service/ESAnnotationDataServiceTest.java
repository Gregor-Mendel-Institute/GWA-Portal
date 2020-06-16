package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAlleleInfo;
import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnotation;
import com.gmi.nordborglab.browser.server.data.annotation.SNPInfo;
import com.gmi.nordborglab.browser.server.domain.pages.SNPInfoPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
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

    private final SNPAnnotation annotationFor4880 = new SNPAnnotation("SYNONYMOUS_CODING", "LOW", "SILENT", "Ctg/Ttg", "L244", "AT1G01010", "AT1G01010.1", 4);

    @Resource
    private TraitUomRepository traitUomRepository;

    @Before
    public void setUp() {


    }

    @After
    public void clearContext() {
    }

    @Test
    public void testGetSNPAnnotations() {
        int[] positions = {4900, 5599, 5731};
        List<SNPInfo> annotations = annotationDataService.getSNPAnnotations("chr1", positions);
        assertNotNull(annotations);
        assertEquals(positions.length, annotations.size());
        assertEquals(annotations.get(0).getAnnotations().get(0).getEffect(), "SYNONYMOUS_CODING");
        assertEquals(annotations.get(1).getAnnotations().get(0).getEffect(), "NON_SYNONYMOUS_CODING");
        assertEquals(annotations.get(2).getAnnotations().get(0).getEffect(), "UTR_3_PRIME");
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
    public void testGetSNPAlleleInfoForAllPassportsWithPassportInfos() {
        SNPAlleleInfo info = annotationDataService.getSNPAlleleInfo(1L, 1, 4880, null, true);
        assertThat("null returned", info, Is.is(notNullValue()));
        assertThat("wrong number of elements", info.getAlleles().size(), is(1386));
        assertThat(info.getPassports().size(), is(1386));
        assertThat(info.getPassports().get(0).getId(), is(9381L));
        assertThat(info.getPassports().get(info.getPassports().size() - 1).getId(), is(9127L));
        checkSNPInfo(info.getSnpInfo());

    }

    @Test
    public void testGetSNPAlleleInfo() {
        List<Long> passportIds = Lists.newArrayList(7000L, 9381L);
        SNPAlleleInfo info = annotationDataService.getSNPAlleleInfo(1L, 1, 4880, passportIds, true);
        assertThat("null returned", info, Is.is(notNullValue()));
        assertThat("wrong number of elements", info.getAlleles().size(), is(2));
        assertThat(info.getPassports().size(), is(2));
        assertThat(info.getPassports().get(0).getId(), is(Long.valueOf(passportIds.get(0))));
        assertThat(info.getPassports().get(1).getId(), is(Long.valueOf(passportIds.get(1))));
        checkSNPInfo(info.getSnpInfo());
    }

    @Test(expected = RuntimeException.class)
    public void test_GetSNPInfosRuntimeExceptionWhenNoAlleleAssayid() {
        annotationDataService.getSNPInfosForFilter(null, null, 0, 10, null);
    }

    @Test(expected = RuntimeException.class)
    public void test_GetSNPInfosRuntimeExceptionWhenInValidRegion() {
        annotationDataService.getSNPInfosForFilter(1L, "WRONG REGION", 0, 10, null);
    }

    @Test
    public void test_GetSNPInfosForGene() {
        SecurityUtils.setAnonymousUser();
        Integer[] refCounts = new Integer[]{1134, 1104, 1133, 1128, 1097};
        Integer[] altCounts = new Integer[]{1, 31, 2, 7, 38};
        SNPInfoPage page = annotationDataService.getSNPInfosForFilter(2L, "AT1G74929", 0, 10, null);
        assertThat(page, notNullValue());
        assertThat(page.getTotalElements(), is(5L));
        assertThat(page.getContents().size(), is(5));
        assertThat(page.getFacets(), notNullValue());
        assertThat(page.getFacets().size(), is(4));
        assertThat(page.getFacets().get(0).getTerms().size(), is(1));
        assertThat(page.getFacets().get(1).getTerms().size(), is(3));
        assertThat(page.getFacets().get(2).getTerms().size(), is(3));
        assertThat(page.getFacets().get(3).getTerms().size(), is(3));
        for (int i = 0; i < page.getContents().size(); i++) {
            SNPInfo info = page.getContents().get(i);
            assertThat(info.getAltCount(), is(altCounts[i]));
            assertThat(info.getRefCount(), is(refCounts[i]));
        }
    }

    @Test
    public void test_GetSNPInfosForRegion() {
        SecurityUtils.setAnonymousUser();
        SNPInfoPage page = annotationDataService.getSNPInfosForFilter(1L, "Chr1:324698-328563", 0, 10, null);
        assertThat(page, notNullValue());
        assertThat(page.getTotalElements(), is(175L));
        assertThat(page.getContents().size(), is(10));
        assertThat(page.getFacets(), notNullValue());
        assertThat(page.getFacets().size(), is(4));
        assertThat(page.getFacets().get(0).getTerms().size(), is(2));
        assertThat(page.getFacets().get(1).getTerms().size(), is(6));
        assertThat(page.getFacets().get(2).getTerms().size(), is(2));
        assertThat(page.getFacets().get(3).getTerms().size(), is(3));
        for (SNPInfo info : page.getContents()) {
            assertThat(info.getChr(), is("1"));
        }
    }

    @Test
    public void test_GetSNPInfosForGeneAndPhenotype() {
        SecurityUtils.setAnonymousUser();
        Integer[] refCounts = new Integer[]{113, 113, 112, 113, 113};
        Integer[] altCounts = new Integer[]{0, 0, 1, 0, 0};
        List<Long> passportIds = FluentIterable.from(traitUomRepository.findOne(1L).getTraits())
                .transform(new Function<Trait, Long>() {
                    @Nullable
                    @Override
                    public Long apply(Trait input) {
                        return input.getObsUnit().getStock().getPassport().getId();
                    }
                }).toList();
        SNPInfoPage page = annotationDataService.getSNPInfosForFilter(2L, "AT1G74929", 0, 10, passportIds);
        assertThat(page, notNullValue());
        assertThat(page.getTotalElements(), is(5L));
        assertThat(page.getContents().size(), is(5));
        for (int i = 0; i < page.getContents().size(); i++) {
            SNPInfo info = page.getContents().get(i);
            assertThat(info.getAltCount(), is(altCounts[i]));
            assertThat(info.getRefCount(), is(refCounts[i]));
            assertThat(info.getChr(), is("1"));
        }

    }

    private void checkSNPInfo(SNPInfo snpInfo) {
        assertThat(snpInfo.getAnnotations(), is(notNullValue()));
        List<SNPAnnotation> annotations = snpInfo.getAnnotations();
        assertThat(annotations.size(), is(2));
        SNPAnnotation annotation = annotations.get(0);
        assertThat(annotation, is(annotationFor4880));
        assertThat(annotations.size(), is(2));
        assertThat(snpInfo.getRef(), is("C"));
        assertThat(snpInfo.getAlt(), is("T"));
    }


}