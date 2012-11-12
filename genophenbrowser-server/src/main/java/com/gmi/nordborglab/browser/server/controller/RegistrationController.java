package com.gmi.nordborglab.browser.server.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gmi.nordborglab.browser.server.form.Registration;
import com.gmi.nordborglab.browser.server.service.DuplicateRegistrationException;
import com.gmi.nordborglab.browser.server.service.UserService;

@Controller
@RequestMapping("/registration")
public class RegistrationController {
	
	@Resource
	UserService userService;

	@RequestMapping(method = RequestMethod.GET)
    public String showRegistration(ModelMap model) {
            Registration registration = new Registration();
            model.addAttribute("registration", registration);
            return "registrationform";
    }
	
	@RequestMapping(method = RequestMethod.POST) 
	public String processForm(@Valid @ModelAttribute(value="registration") Registration registration,BindingResult result)	 {
		verifyBinding(result);
		
		try {
			userService.registerUserIfValid(registration, !result.hasErrors());
		}catch (DuplicateRegistrationException e)  {
			result.rejectValue("email","not.unique");
		}
		
		if (result.hasErrors()) { 
			return "registrationform";
		}
		
		return "/index.jsp";
	}
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
            binder.setAllowedFields(new String[] { 
                            "firstname", "lastname", "email",
                             "password", "confirmPassword"
            });
    }
	
	private void verifyBinding(BindingResult result) {
        String[] suppressedFields = result.getSuppressedFields();
        if (suppressedFields.length > 0) {
                throw new RuntimeException("You've attempted to bind fields that haven't been allowed in initBinder(): " 
                                + StringUtils.join(suppressedFields, ", "));
        }
	}
	
}
