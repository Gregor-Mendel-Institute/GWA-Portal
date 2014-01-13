package com.gmi.nordborglab.browser.server.controller;

import com.gmi.nordborglab.browser.server.service.HelperService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;


@Controller
@RequestMapping("/")
public class MainController {

    @Resource
    protected HelperService helperService;

    @Value("${JBROWSE.url}")
    private String jbrowseUrl;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView result = new ModelAndView("index");
        result.addObject("appData", helperService.getAppData());
        result.addObject("jbrowseUrl", jbrowseUrl);
        return result;
    }
}
