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

    @RequestMapping(method =  {RequestMethod.GET, RequestMethod.HEAD})
    public ModelAndView index() {
        ModelAndView result = new ModelAndView("index");
        result.addObject("jbrowseUrl", jbrowseUrl);
        result.addObject("gaTrackingId", gaTrackingId);
        result.addObject("contactEmail", contactEmail);
        result.addObject("geneInfoUrl",geneInfoUrl);
        return result;
    }
}
