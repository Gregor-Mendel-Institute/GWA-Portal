package com.gmi.nordborglab.browser.server.controller;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.service.MailService;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 28.11.13
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping()
public class ForgotPasswordController {

    private final static String RESET_PAGE = "password_reset";
    private final static String FORGOT_PAGE = "forgot_password";

    public static class PasswordForgot {

        @NotEmpty
        @Email
        private String email;

        private String status;


        public PasswordForgot() {
        }


        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class PasswordReset {

        private String status;
        @NotEmpty
        private String token;

        @NotEmpty
        @Size(min = 4, max = 20)
        private String password;

        @NotEmpty
        private String confirmPassword;

        public PasswordReset() {
        }

        public PasswordReset(String token) {
            this.token = token;
        }

        @AssertTrue(message = "Password does not match")
        public boolean isPasswordValid() {
            if (password == null) {
                return false;
            } else {
                return password.equals(confirmPassword);
            }
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    @Resource
    UserRepository userRepository;

    @Resource
    MailService mailService;

    @Resource
    protected PasswordEncoder encoder;


    @RequestMapping(value = "/reset-password", method = RequestMethod.GET)
    public String showResetPassword(ModelMap model, @RequestParam("token") String token) {
        PasswordReset passwordReset = new PasswordReset(token);
        model.addAttribute("reset_password", passwordReset);
        return RESET_PAGE;
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.GET)
    public String showForgotPassword(ModelMap model) {
        PasswordForgot passwordForgot = new PasswordForgot();
        model.addAttribute("password_forgot", passwordForgot);
        return FORGOT_PAGE;
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public String resetPassword(@Valid @ModelAttribute(value = "reset_password") PasswordReset passwordReset, BindingResult result) {

        verifyBinding(result);
        if (result.hasErrors()) {
            return RESET_PAGE;
        }
        String token = passwordReset.getToken();

        if (token == null || token.isEmpty()) {
            result.reject("", "token.missing");
            return RESET_PAGE;
        }
        try {
            AppUser user = userRepository.findByPasswordResetToken(token);
            if (user == null) {
                result.reject("", "token.used");
                return RESET_PAGE;
            }

            Date currentDate = new Date();
            if (currentDate.getTime() > user.getPasswordResetExpiration().getTime()) {
                result.reject("", "token.expired");
                return RESET_PAGE;
            }
            user.setPassword(encoder.encode(passwordReset.getPassword()));
            user.setPasswordResetToken(null);
            userRepository.save(user);
            mailService.sendPasswordChanged(user);
            passwordReset.setStatus("Password was successfully reset");
            token = null;
            passwordReset = null;
        } catch (Exception e) {
            result.rejectValue("", "general.error");
        }
        return RESET_PAGE;
    }

    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public String sendPasswordResetLink(@Valid @ModelAttribute(value = "password_forgot") PasswordForgot passwordForgot, BindingResult result) {

        verifyBinding(result);
        if (result.hasErrors()) {
            return FORGOT_PAGE;
        }

        try {
            AppUser user = userRepository.findByEmail(passwordForgot.getEmail());
            String passwordResetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(passwordResetToken);
            userRepository.save(user);
            mailService.sendPasswordResetLink(user);
            passwordForgot.setStatus("Mail with password-reset link was sent to your email address");

        } catch (Exception e) {
            result.rejectValue("", "general.error");
        }
        return FORGOT_PAGE;
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[]{
                "email", "password", "confirmPassword", "token"
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
