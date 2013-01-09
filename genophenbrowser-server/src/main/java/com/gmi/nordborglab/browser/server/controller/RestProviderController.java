package com.gmi.nordborglab.browser.server.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.TraitService;
import com.google.common.base.Joiner;


@Controller
@RequestMapping("/provider")
public class RestProviderController {
	
	@Resource
	private CdvService cdvService;
	
	@Resource
	private TraitService traitService;
	
	@RequestMapping(method=RequestMethod.GET,value="/study/{id}/phenotypedata")
	public @ResponseBody String getPhenotypeData(@PathVariable("id") Long id) {
		List<Trait> traits = cdvService.findTraitValues(id);
		String csvData = null;
		StringBuilder builder = new StringBuilder();
		Joiner joiner = Joiner.on(",").useForNull("NA");
		builder.append(joiner.join("ecotypeId",id));
		for (Trait trait:traits) {
			builder.append("\n").append(joiner.join(trait.getObsUnit().getStock().getPassport().getId(),trait.getValue())); 
		}
		csvData = builder.toString();
		return csvData;
	}
	
	@RequestMapping(method=RequestMethod.GET,value="/phenotype/{id}/phenotypedata/{statisticsTypeId}")
	public @ResponseBody String getPhenotypeData(@PathVariable("id") Long id,@PathVariable("statisticsTypeId") Long statisticsTypeId) {
		List<Trait> traits = traitService.findAllTraitValuesByStatisticType(id, statisticsTypeId);
		String csvData = null;
		StringBuilder builder = new StringBuilder();
		Joiner joiner = Joiner.on(",").useForNull("NA");
		builder.append(joiner.join("ecotypeId",id));
		for (Trait trait:traits) {
			builder.append("\n").append(joiner.join(trait.getObsUnit().getStock().getPassport().getId(),trait.getValue())); 
		}
		csvData = builder.toString();
		return csvData;
	}
	
}
