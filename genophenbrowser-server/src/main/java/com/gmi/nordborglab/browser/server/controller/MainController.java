package com.gmi.nordborglab.browser.server.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.gmi.nordborglab.browser.server.service.HelperService;


@Controller
@RequestMapping("/")
public class MainController {
	
	@Resource
	protected HelperService helperService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView index() {
		ModelAndView result = new ModelAndView("index");
		result.addObject("appData", helperService.getAppData());
	    return result;
	}
}
