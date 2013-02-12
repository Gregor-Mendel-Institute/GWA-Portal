package com.gmi.nordborglab.browser.server.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.StudyGWASData;
import com.gmi.nordborglab.browser.server.service.HelperService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.TraitService;
import com.google.common.base.Joiner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


@Controller
@RequestMapping("/provider")
public class RestProviderController {
	
	@Resource
	private CdvService cdvService;
	
	@Resource
	private TraitService traitService;

    @Resource
    private HelperService helperService;
	
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

    @RequestMapping(method = RequestMethod.GET,value="/study/{id}/studygwasdata")
    public @ResponseBody
    StudyGWASData getStudyGWASData(@PathVariable("id") Long id) {
        Study study = cdvService.findStudy(id);
        List<Trait> traits = cdvService.findTraitValues(id);
        String csvData = null;
        StringBuilder builder = new StringBuilder();
        Joiner joiner = Joiner.on(",").useForNull("NA");
        builder.append(joiner.join("ecotypeId",id));
        for (Trait trait:traits) {
            builder.append("\n").append(joiner.join(trait.getObsUnit().getStock().getPassport().getId(),trait.getValue()));
        }
        csvData = builder.toString();
        //todo mapping between alleleassay and genotype
        StudyGWASData data = new StudyGWASData(csvData,study.getProtocol().getAnalysisMethod(),82);
        return data;
    }


    @RequestMapping(method = RequestMethod.POST,value="/phenotype/upload")
    public @ResponseBody
    PhenotypeUploadData uploadPhenotype(@RequestParam("file")CommonsMultipartFile file) {
        PhenotypeUploadData data = null;
        try {
            byte[] csvData = IOUtils.toByteArray(file.getInputStream());
            data = helperService.getPhenotypeUploadData(csvData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        }
        return data;
    }


}
