package com.gmi.nordborglab.browser.client.validation;

import com.google.common.collect.Sets;
import com.google.gwt.editor.client.EditorError;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.11.13
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class ClientValidation extends Validation {


    private Validator validator;
    private Set<ConstraintViolation<?>> constraintViolations;

    public ClientValidation() {
        this.validator = buildDefaultValidatorFactory().getValidator();
    }

    public Set<ConstraintViolation<?>> getConstraintViolations(Object object, List<EditorError> editorErrors) {
        Set<ConstraintViolation<?>> constraintViolations = Sets.newHashSet();
        constraintViolations.addAll(validator.validate(object));
        if (editorErrors != null && !editorErrors.isEmpty()) {
            for (EditorError error : editorErrors) {
                constraintViolations.add(new EditorErrorConstraintViolation<Object>(error));
            }
        }
        return constraintViolations;
    }

    public String getPrimaryMessage() {
        return constraintViolations.iterator().next().getMessage();
    }
}