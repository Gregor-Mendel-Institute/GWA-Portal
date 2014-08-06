package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitStats;
import com.gmi.nordborglab.browser.server.testutils.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by uemit.seren on 8/4/14.
 */
public class TraitStatsRepositoryTest extends BaseTest {

    @Resource
    protected TraitStatsRepository repository;

    @Test
    public void testFindByTraitUomIdAndStatisticTypeId() {
        List<TraitStats> traitStatsList = repository.findByTraitUomIdAndStatisticTypeId(1L, 2L);
        assertThat(traitStatsList, is(notNullValue()));
        assertThat(traitStatsList.size(), is(167));
    }

}