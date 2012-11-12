package com.gmi.nordborglab.browser.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/login")
public class LoginController {
	
	@RequestMapping(method = RequestMethod.GET)
	public String login(HttpServletRequest request,ModelMap model) {
		if (request.getParameter("url") != null && request.getParameter("") != "") 
			model.addAttribute("url", "#"+ request.getParameter("url"));
	    return "login";
	}

}
