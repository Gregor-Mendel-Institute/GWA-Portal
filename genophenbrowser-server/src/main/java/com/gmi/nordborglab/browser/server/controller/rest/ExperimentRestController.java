package com.gmi.nordborglab.browser.server.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.gmi.nordborglab.browser.server.controller.rest.exceptions.ResourceNotFoundException;
import com.gmi.nordborglab.browser.server.controller.rest.json.Views;
import com.gmi.nordborglab.browser.server.controller.rest.util.PaginationUtil;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
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
@RequestMapping("/api/studies")
public class ExperimentRestController {

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private TraitUomService traitUomService;

    @RequestMapping(method = RequestMethod.GET)
    @JsonView(Views.Experiments.class)
    ResponseEntity<Collection<Experiment>> getExperiments(@RequestParam(defaultValue = "0", value = "page") int page, @RequestParam(defaultValue = "50", value = "size") int size) {
        Page<Experiment> experimentPage = experimentService.findByAclAndFilter(ConstEnums.TABLE_FILTER.ALL, null, page, size);
        HttpHeaders headers = null;
        if (page > experimentPage.getTotalPages()) {
            throw new ResourceNotFoundException("No data found for the selected page");
        }
        try {
            headers = PaginationUtil.generatePaginationHttpHeaders(experimentPage, "/api/studies", page, size);
        } catch (URISyntaxException e) {

        }
        return new ResponseEntity<Collection<Experiment>>(experimentPage.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{experimentId}", method = RequestMethod.GET)
    @JsonView(Views.ExperimentDetail.class)
    ResponseEntity<Experiment> getExperiment(@PathVariable Long experimentId) {
        Experiment experiment = experimentService.findExperiment(experimentId);
        if (experiment == null)
            throw new ResourceNotFoundException("Experiment not found");
        return new ResponseEntity<>(experiment, HttpStatus.OK);
    }

    @RequestMapping(value = "/{experimentId}/phenotypes", method = RequestMethod.GET)
    @JsonView(Views.ExperimentDetail.class)
    ResponseEntity<Collection<TraitUom>> getPhenotypesOfExperiment(@PathVariable Long experimentId, @RequestParam(defaultValue = "0", value = "page") int page, @RequestParam(defaultValue = "50", value = "size") int size) {
        Page<TraitUom> traitUomPage = traitUomService.findAll(experimentId, ConstEnums.TABLE_FILTER.ALL, null, page, size);
        HttpHeaders headers = null;
        if (page > traitUomPage.getTotalPages()) {
            throw new ResourceNotFoundException("No data found for the selected page");
        }
        try {
            headers = PaginationUtil.generatePaginationHttpHeaders(traitUomPage, "/api/studies/" + experimentId + "/phenotypes", page, size);
        } catch (URISyntaxException e) {

        }
        return new ResponseEntity<Collection<TraitUom>>(traitUomPage.getContent(), headers, HttpStatus.OK);
    }

}
