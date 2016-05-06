package com.gmi.nordborglab.browser.server.controller.rest;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
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
 * Created by uemit.seren on 7/7/15.
 */
public class PhenotypeRestControllerTest extends AbstractRestControllerTest {


    @Autowired
    TraitUomService traitUomService;


    @Before
    public void setUp() {
        reset(traitUomService);
        SecurityUtils.setAnonymousUser();
    }


    public static TraitUomPage getFakeTraitUomPage() {
        List<TraitUom> traits = Lists.newArrayList();
        traits.add(getFakeTraitUom(1L));
        traits.add(getFakeTraitUom(2L));
        return new TraitUomPage(traits, new PageRequest(0, 50), 2, null);
    }

    public static TraitUom getFakeTraitUom(Long id) {
        UnitOfMeasure unitOfMeasure = new UnitOfMeasure();
        unitOfMeasure.setId(id);
        unitOfMeasure.setUnitType("mm");
        Experiment experiment = new Experiment();
        experiment.setId(1L);
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
        return traitUom;
    }

    @Test
    public void getPhenotypes_ShouldReturnListWithPagingHeader() throws Exception {
        TraitUomPage page = getFakeTraitUomPage();
        given(traitUomService.findAll(any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/phenotypes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string(HttpHeaders.LINK, "</api/phenotypes?page=1&per_page=50>; rel=\"last\",</api/phenotypes?page=1&per_page=50>; rel=\"first\""))
                        //.andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].localTraitName", is("Trait1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].localTraitName", is("Trait2")));

        verify(traitUomService, times(1)).findAll(eq(ConstEnums.TABLE_FILTER.ALL), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(traitUomService);
    }

    @Test
    public void getPhenotypes_ReturnNotFoundWhenPageIsBiggerThanTotalPageNumbers() throws Exception {
        TraitUomPage page = getFakeTraitUomPage();
        given(traitUomService.findAll(any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/phenotypes?page=2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));

        verify(traitUomService, times(1)).findAll(eq(ConstEnums.TABLE_FILTER.ALL), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(traitUomService);
    }

    @Test
    public void getSinglePhenotype_ShouldReturn() throws Exception {
        TraitUom traitUom = getFakeTraitUom(1L);
        given(traitUomService.findPhenotype(1L)).willReturn(traitUom);
        mockMvc.perform(get("/api/phenotypes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.localTraitName", is("Trait1")));
        verify(traitUomService, times(1)).findPhenotype(eq(1L));
        verifyNoMoreInteractions(traitUomService);
    }

    @Test
    public void getSinglePhenotype_ReturnNotfoundIfDoesNotExist() throws Exception {
        given(traitUomService.findPhenotype(1L)).willReturn(null);
        mockMvc.perform(get("/api/phenotypes/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));
        verify(traitUomService, times(1)).findPhenotype(eq(1L));
        verifyNoMoreInteractions(traitUomService);
    }

    @Test
    public void getStudyOfPhenotype_ShouldReturn() throws Exception {
        TraitUom traitUom = getFakeTraitUom(1L);
        given(traitUomService.findPhenotype(1L)).willReturn(traitUom);
        mockMvc.perform(get("/api/phenotypes/1/study"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Experiment1")));
        verify(traitUomService, times(1)).findPhenotype(eq(1L));
        verifyNoMoreInteractions(traitUomService);

    }

    @Test
    public void getStudyOfPhenotype_ReturnNotfoundIfDoesNotExist() throws Exception {
        given(traitUomService.findPhenotype(1L)).willReturn(null);
        mockMvc.perform(get("/api/phenotypes/1/study"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));
        verify(traitUomService, times(1)).findPhenotype(eq(1L));
        verifyNoMoreInteractions(traitUomService);
    }

}
