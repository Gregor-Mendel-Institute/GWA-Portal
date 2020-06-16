package com.gmi.nordborglab.browser.server.controller.rest;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.gmi.nordborglab.browser.server.testutils.AbstractRestControllerTest;
import com.gmi.nordborglab.browser.server.testutils.SecurityUtils;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by uemit.seren on 7/8/15.
 */
public class ExperimentRestControllerTest extends AbstractRestControllerTest {

    @Autowired
    ExperimentService experimentService;

    @Autowired //TODO should be moved to ExperimentService
            TraitUomService traitUomService;

    @Before
    public void setUp() {
        reset(experimentService);
        reset(traitUomService);
        SecurityUtils.setAnonymousUser();
    }


    private ExperimentPage getFakeExperimentPage() {
        List<Experiment> experiments = Lists.newArrayList();
        experiments.add(getFakeExperiment(1L));
        experiments.add(getFakeExperiment(2L));
        return new ExperimentPage(experiments, new PageRequest(0, 50), 2, null);
    }

    private Experiment getFakeExperiment(Long id) {
        UnitOfMeasure unitOfMeasure = new UnitOfMeasure();
        unitOfMeasure.setId(id);
        unitOfMeasure.setUnitType("mm");
        Experiment experiment = new Experiment();
        experiment.setId(id);
        experiment.setName("Experiment" + id);
        Trait trait = new Trait();
        ObsUnit obsUnit = new ObsUnit();
        obsUnit.setExperiment(experiment);
        trait.setObsUnit(obsUnit);
        TraitUom traitUom = new TraitUom();
        traitUom.setId(id);
        traitUom.setUnitOfMeasure(unitOfMeasure);
        traitUom.setLocalTraitName("Trait" + id);
        traitUom.setTraitProtocol("Trait+" + id + " Protocol");
        traitUom.addTrait(trait);
        return experiment;
    }

    @Test
    public void getExperiments_ShouldReturnListWithPagingHeader() throws Exception {
        ExperimentPage page = getFakeExperimentPage();
        given(experimentService.findByAclAndFilter(any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/studies"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string(HttpHeaders.LINK, "</api/studies?page=1&per_page=50>; rel=\"last\",</api/studies?page=1&per_page=50>; rel=\"first\""))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Experiment1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Experiment2")));

        verify(experimentService, times(1)).findByAclAndFilter(eq(ConstEnums.TABLE_FILTER.ALL), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(experimentService);
    }

    @Test
    public void getExperiments_ReturnNotFoundWhenPageIsBiggerThanTotalPageNumbers() throws Exception {
        ExperimentPage page = getFakeExperimentPage();
        given(experimentService.findByAclAndFilter(any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/studies?page=2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));

        verify(experimentService, times(1)).findByAclAndFilter(eq(ConstEnums.TABLE_FILTER.ALL), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(experimentService);
    }

    @Test
    public void getSingleExperiment_ShouldReturn() throws Exception {
        Experiment experiment = getFakeExperiment(1L);
        given(experimentService.findExperiment(1L)).willReturn(experiment);
        mockMvc.perform(get("/api/studies/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Experiment1")));
        verify(experimentService, times(1)).findExperiment(eq(1L));
        verifyNoMoreInteractions(experimentService);
    }

    @Test
    public void getSingleExperiment_ReturnNotfoundIfDoesNotExist() throws Exception {
        given(experimentService.findExperiment(1L)).willReturn(null);
        mockMvc.perform(get("/api/studies/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));
        verify(experimentService, times(1)).findExperiment(eq(1L));
        verifyNoMoreInteractions(experimentService);
    }

    @Test
    public void getPhenotypesOfExperiment_ShouldReturn() throws Exception {
        TraitUomPage page = PhenotypeRestControllerTest.getFakeTraitUomPage();
        given(traitUomService.findAll(eq(1L), any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/studies/1/phenotypes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string(HttpHeaders.LINK, "</api/studies/1/phenotypes?page=1&per_page=50>; rel=\"last\",</api/studies/1/phenotypes?page=1&per_page=50>; rel=\"first\""))
                        //.andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].localTraitName", is("Trait1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].localTraitName", is("Trait2")));
        verify(traitUomService, times(1)).findAll(eq(1L), any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(traitUomService);
    }
}
