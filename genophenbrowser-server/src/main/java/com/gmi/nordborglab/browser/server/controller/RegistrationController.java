package com.gmi.nordborglab.browser.server.controller;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.form.Registration;
import com.gmi.nordborglab.browser.server.service.DuplicateRegistrationException;
import com.gmi.nordborglab.browser.server.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Resource;
import javax.validation.Valid;

@Controller
@RequestMapping("/registration")
public class RegistrationController {

    @Resource
    UserService userService;

    private final ProviderSignInUtils providerSignInUtils = new ProviderSignInUtils();

    @RequestMapping(method = RequestMethod.GET)
    public String showRegistration(WebRequest request, ModelMap model) {
        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
        Registration registration = createRegistrationDTO(connection);
        model.addAttribute("registration", registration);
        return "registrationform";
    }

    private Registration createRegistrationDTO(Connection<?> connection) {
        Registration dto = new Registration();

        if (connection != null) {
            UserProfile socialMediaProfile = connection.fetchUserProfile();
            dto.setEmail(socialMediaProfile.getEmail());
            dto.setFirstname(socialMediaProfile.getFirstName());
            dto.setLastname(socialMediaProfile.getLastName());
        }

        return dto;
    }

    // BindingResult must come after Registration: http://stackoverflow.com/questions/15622098/how-do-i-display-a-user-friendly-error-when-spring-is-unable-to-validate-a-model
    @RequestMapping(method = RequestMethod.POST)
    public String processForm(@Valid @ModelAttribute(value = "registration") Registration registration, BindingResult result, WebRequest request) {
        verifyBinding(result);
        if (result.hasErrors()) {
            return "registrationform";
        }
        try {
            AppUser user = userService.registerUserIfValid(registration, !result.hasErrors());
            providerSignInUtils.doPostSignUp(user.getUsername(), request);
        } catch (DuplicateRegistrationException e) {
            result.rejectValue("email", "not.unique");
        }
        // because of  http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-redirecting
        return "redirect:/";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[]{
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
