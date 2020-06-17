package com.gmi.nordborglab.browser.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/")
public class MainController {

    @Value("${JBROWSE.url}")
    private String jbrowseUrl;

    @Value("${GA.trackingid}")
    private String gaTrackingId;

    @Value("${SITE.contactemail}")
    private String contactEmail;

    @Value("${GENE.info_url}")
    private String geneInfoUrl = "";

    @Value("${GENOME.chromosomes}")
    private String chromosomes = "";

    @Value("#{environment.VERSION}")
    private String version;

    @Value("#{environment.COMMIT_HASH}")
    private String commitHash;

    @Value("${SITE.mapsApiKey}")
    private String mapsApiKey;

    @RequestMapping(method =  {RequestMethod.GET, RequestMethod.HEAD})
    public ModelAndView index() {

        ModelAndView result = new ModelAndView("index");
        result.addObject("jbrowseUrl", jbrowseUrl);
        result.addObject("gaTrackingId", gaTrackingId);
        result.addObject("contactEmail", contactEmail);
        result.addObject("geneInfoUrl",geneInfoUrl);
        result.addObject("chromosomes", chromosomes);
        result.addObject("version", version);
        result.addObject("commitHash", commitHash);
        result.addObject("mapsApiKey", mapsApiKey);

        return result;
    }
}
