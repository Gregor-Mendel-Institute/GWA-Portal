package com.gmi.nordborglab.browser.shared.validation;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.11.13
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class PasswordsEqualValidator implements ConstraintValidator<PasswordsEqual, AppUserProxy> {

    @Override
    public void initialize(PasswordsEqual constraintAnnotation) {

    }

    @Override
    public boolean isValid(AppUserProxy value, ConstraintValidatorContext context) {

        if (value == null)
            return true;

        String password = value.getNewPassword();
        String confirmPassword = value.getNewPasswordConfirm();
        if (password == null && confirmPassword == null)
            return true;

        if (password != null && password.equals(confirmPassword))
            return true;

        // http://pwinckles.blogspot.co.at/2011/05/multi-field-bean-validation-jsr-303.html
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addNode("newPasswordConfirm").addConstraintViolation();
        context.disableDefaultConstraintViolation();
        return false;
    }


}

