package com.gmi.nordborglab.browser.server.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.gmi.nordborglab.browser.server.controller.rest.exceptions.ResourceNotFoundException;
import com.gmi.nordborglab.browser.server.controller.rest.json.Views;
import com.gmi.nordborglab.browser.server.controller.rest.util.PaginationUtil;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Created by uemit.seren on 7/8/15.
 */
@RestController
@RequestMapping("/api/analyses")
public class CdvRestController {

    @Autowired
    private CdvService cdvService;

    @RequestMapping(method = RequestMethod.GET)
    @JsonView(Views.Studies.class)
    ResponseEntity<Collection<Study>> getAnalyses(@RequestParam(defaultValue = "0", value = "page") int page, @RequestParam(defaultValue = "50", value = "size") int size) {
        Page<Study> studyPage = cdvService.findAll(ConstEnums.TABLE_FILTER.ALL, null, page, size);
        HttpHeaders headers = null;
        if (page > studyPage.getTotalPages()) {
            throw new ResourceNotFoundException("No data found for the selected page");
        }
        try {
            headers = PaginationUtil.generatePaginationHttpHeaders(studyPage, "/api/analyses", page, size);
        } catch (URISyntaxException e) {

        }
        return new ResponseEntity<Collection<Study>>(studyPage.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{analysisId}", method = RequestMethod.GET)
    @JsonView(Views.StudyDetail.class)
    ResponseEntity<Study> getAnalysis(@PathVariable Long analysisId) {
        Study study = cdvService.findStudy(analysisId);
        if (study == null)
            throw new ResourceNotFoundException("Study not found");
        return new ResponseEntity<>(study, HttpStatus.OK);
    }

    @RequestMapping(value = "/{analysisId}/phenotype", method = RequestMethod.GET)
    @JsonView(Views.PhenotypeDetail.class)
    ResponseEntity<TraitUom> getPhenotypeOfAnalysis(@PathVariable Long analysisId) {
        Study study = cdvService.findStudy(analysisId);
        if (study == null)
            throw new ResourceNotFoundException("Study not found");
        return new ResponseEntity<>(study.getPhenotype(), HttpStatus.OK);
    }

}
