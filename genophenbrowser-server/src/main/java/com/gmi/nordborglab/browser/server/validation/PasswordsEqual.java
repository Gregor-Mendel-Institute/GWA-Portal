package com.gmi.nordborglab.browser.server.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.11.13
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsEqualValidator.class)
@Documented
public @interface PasswordsEqual {
    String DEFAULT_MESSAGE = "Passwords must be the same";

    String message() default DEFAULT_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
