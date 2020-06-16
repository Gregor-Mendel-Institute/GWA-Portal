package com.gmi.nordborglab.browser.server.controller.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.gmi.nordborglab.browser.server.controller.rest.exceptions.ResourceNotFoundException;
import com.gmi.nordborglab.browser.server.controller.rest.json.Views;
import com.gmi.nordborglab.browser.server.controller.rest.util.PaginationUtil;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
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
 * Created by uemit.seren on 7/6/15.
 */
@RestController
@RequestMapping("/api/phenotypes")
public class PhenotypeRestController {

    @Autowired
    private TraitUomService traitUomService;


    @RequestMapping(method = RequestMethod.GET)
    @JsonView(Views.Phenotypes.class)
    ResponseEntity<Collection<TraitUom>> getPhenotypes(@RequestParam(defaultValue = "0", value = "page") int page, @RequestParam(defaultValue = "50", value = "size") int size) {
        Page<TraitUom> phenotypePage = traitUomService.findAll(ConstEnums.TABLE_FILTER.ALL, null, page, size);
        HttpHeaders headers = null;
        if (page > phenotypePage.getTotalPages()) {
            throw new ResourceNotFoundException("No data found for the selected page");
        }
        try {
            headers = PaginationUtil.generatePaginationHttpHeaders(phenotypePage, "/api/phenotypes", page, size);
        } catch (URISyntaxException e) {

        }
        return new ResponseEntity<Collection<TraitUom>>(phenotypePage.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{phenotypeId}", method = RequestMethod.GET)
    @JsonView(Views.PhenotypeDetail.class)
    ResponseEntity<TraitUom> getPhenotype(@PathVariable Long phenotypeId) {
        TraitUom trait = traitUomService.findPhenotype(phenotypeId);
        if (trait == null)
            throw new ResourceNotFoundException("Phenotype not found");
        return new ResponseEntity<>(trait, HttpStatus.OK);
    }

    @RequestMapping(value = "/{phenotypeId}/study", method = RequestMethod.GET)
    @JsonView(Views.ExperimentDetail.class)
    ResponseEntity<Experiment> getStudyOfPhenotype(@PathVariable Long phenotypeId) {
        TraitUom trait = traitUomService.findPhenotype(phenotypeId);
        if (trait == null)
            throw new ResourceNotFoundException("Phenotype not found");
        return new ResponseEntity<>(trait.getExperiment(), HttpStatus.OK);
    }
}
