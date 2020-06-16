package com.gmi.nordborglab.browser.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TestController {
	
	@RequestMapping(value="/test",method = RequestMethod.GET)
	public String test() {
		return "registrationform";
	}
	
	@RequestMapping(value="/genophenbrowser/test",method = RequestMethod.GET)
	public String test2() {
		return "registrationform";
	}
}
