package com.gmi.nordborglab.browser.server.controller.rest;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import com.gmi.nordborglab.browser.server.service.CdvService;
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
public class CdvRestControllerTest extends AbstractRestControllerTest {


    @Autowired
    CdvService cdvService;


    @Before
    public void setUp() {
        reset(cdvService);
        SecurityUtils.setAnonymousUser();
    }


    public static StudyPage getFakeStudyPage() {
        List<Study> analyses = Lists.newArrayList();
        analyses.add(getFakeStudy(1L));
        analyses.add(getFakeStudy(2L));
        return new StudyPage(analyses, new PageRequest(0, 50), 2, null);
    }

    public static Study getFakeStudy(Long id) {
        Study study = new Study();
        study.setId(id);
        study.setName("Study" + id);

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
        study.addTrait(trait);
        return study;
    }

    @Test
    public void getAnalyses_ShouldReturnListWithPagingHeader() throws Exception {
        StudyPage page = getFakeStudyPage();
        given(cdvService.findAll(any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/analyses"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string(HttpHeaders.LINK, "</api/analyses?page=1&per_page=50>; rel=\"last\",</api/analyses?page=1&per_page=50>; rel=\"first\""))
                        //.andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Study1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Study2")));

        verify(cdvService, times(1)).findAll(eq(ConstEnums.TABLE_FILTER.ALL), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(cdvService);
    }

    @Test
    public void getAnalyses_ReturnNotFoundWhenPageIsBiggerThanTotalPageNumbers() throws Exception {
        StudyPage page = getFakeStudyPage();
        // or(isNull(String.class) because of https://github.com/mockito/mockito/issues/185
        given(cdvService.findAll(any(ConstEnums.TABLE_FILTER.class), or(isNull(String.class), anyString()), anyInt(), anyInt())).willReturn(page);
        mockMvc.perform(get("/api/analyses?page=2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));

        verify(cdvService, times(1)).findAll(eq(ConstEnums.TABLE_FILTER.ALL), or(isNull(String.class), anyString()), anyInt(), anyInt());
        verifyNoMoreInteractions(cdvService);
    }

    @Test
    public void getSingleAnalysis_ShouldReturn() throws Exception {
        Study study = getFakeStudy(1L);
        given(cdvService.findStudy(1L)).willReturn(study);
        mockMvc.perform(get("/api/analyses/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Study1")));
        verify(cdvService, times(1)).findStudy(eq(1L));
        verifyNoMoreInteractions(cdvService);
    }

    @Test
    public void getSingleAnalysis_ReturnNotfoundIfDoesNotExist() throws Exception {
        given(cdvService.findStudy(1L)).willReturn(null);
        mockMvc.perform(get("/api/analyses/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));
        verify(cdvService, times(1)).findStudy(eq(1L));
        verifyNoMoreInteractions(cdvService);
    }

    @Test
    public void getPhenotypeOfAnalysis_ShouldReturn() throws Exception {
        Study study = getFakeStudy(1L);
        given(cdvService.findStudy(1L)).willReturn(study);
        mockMvc.perform(get("/api/analyses/1/phenotype"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.localTraitName", is("Trait1")));
        verify(cdvService, times(1)).findStudy(eq(1L));
        verifyNoMoreInteractions(cdvService);

    }

    @Test
    public void getStudyOfPhenotype_ReturnNotfoundIfDoesNotExist() throws Exception {
        given(cdvService.findStudy(1L)).willReturn(null);
        mockMvc.perform(get("/api/analyses/1/phenotype"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(contentType));
        verify(cdvService, times(1)).findStudy(eq(1L));
        verifyNoMoreInteractions(cdvService);
    }
}
