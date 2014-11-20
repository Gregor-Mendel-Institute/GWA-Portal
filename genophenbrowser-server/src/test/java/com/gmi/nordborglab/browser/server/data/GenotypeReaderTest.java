package com.gmi.nordborglab.browser.server.data;

import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.google.common.collect.Sets;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by uemit.seren on 10/27/14.
 */
public class GenotypeReaderTest extends BaseTest {

    @Resource
    protected GenotypeReader geneReader;


    @Before
    public void setUp() {
        SecurityUtils.setAnonymousUser();
    }

    @After
    public void clearContext() {
        SecurityUtils.clearContext();
    }

    @Test
    public void testGetAllAllelesFromGenotype() {
        List<Byte> alleles = geneReader.getAlleles("1", 1, 30424323, null);
        assertThat("null returned", alleles, Is.is(notNullValue()));
        assertThat("wrong number of elements", alleles.size(), is(1386));
    }

    @Test
    public void testGetAllelesFromGenotype() {
        LinkedHashSet<String> passportIds = Sets.newLinkedHashSet();
        passportIds.add("7000");
        passportIds.add("9378");
        List<Byte> alleles = geneReader.getAlleles("1", 1, 30424323, passportIds);
        assertThat("null returned", alleles, Is.is(notNullValue()));
        assertThat("wrong number of elements", alleles.size(), is(2));
    }


}
