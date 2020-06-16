package com.gmi.nordborglab.browser.client.validation;

import com.google.gwt.editor.client.EditorError;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 04.11.13
 * Time: 16:11
 * To change this template use File | Settings | File Templates.
 */
public class EditorErrorConstraintViolation<T> implements ConstraintViolation<T> {

    private final EditorError error;

    public EditorErrorConstraintViolation(final EditorError error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error.getMessage();
    }

    @Override
    public String getMessageTemplate() {
        return error.getMessage();
    }

    @Override
    public T getRootBean() {
        return (T) error.getUserData();
    }

    @Override
    public Class<T> getRootBeanClass() {
        return (Class<T>) error.getUserData().getClass();
    }

    @Override
    public Object getLeafBean() {
        return null;
    }

    @Override
    public Path getPropertyPath() {
        return null;
    }

    @Override
    public Object getInvalidValue() {
        return error.getValue();
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}