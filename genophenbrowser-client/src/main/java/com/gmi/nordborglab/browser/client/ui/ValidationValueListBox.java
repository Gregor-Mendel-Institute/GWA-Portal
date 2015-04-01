package com.gmi.nordborglab.browser.client.ui;

import com.google.gwt.editor.client.EditorError;
import com.google.gwt.editor.client.HasEditorErrors;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.view.client.ProvidesKey;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.gwtbootstrap3.client.ui.ValueListBox;
import org.gwtbootstrap3.client.ui.base.mixin.BlankValidatorMixin;
import org.gwtbootstrap3.client.ui.base.mixin.ErrorHandlerMixin;
import org.gwtbootstrap3.client.ui.form.error.ErrorHandler;
import org.gwtbootstrap3.client.ui.form.error.ErrorHandlerType;
import org.gwtbootstrap3.client.ui.form.error.HasErrorHandler;
import org.gwtbootstrap3.client.ui.form.validator.HasBlankValidator;
import org.gwtbootstrap3.client.ui.form.validator.HasValidators;
import org.gwtbootstrap3.client.ui.form.validator.ValidationChangedEvent;
import org.gwtbootstrap3.client.ui.form.validator.Validator;

import java.util.List;

/**
 * Created by uemit.seren on 3/31/15.
 */
// TODO only required until https://github.com/gwtbootstrap3/gwtbootstrap3/issues/343#issuecomment-88402934 fixed
public class ValidationValueListBox<T> extends ValueListBox<T> implements HasEditorErrors<T>, HasErrorHandler, HasValidators<T>, HasBlankValidator<T> {

    private final ErrorHandlerMixin<T> errorHandlerMixin = new ErrorHandlerMixin(this);
    private final BlankValidatorMixin<ValidationValueListBox<T>, T> validatorMixin;

    public ValidationValueListBox(Renderer<T> renderer) {
        super(renderer);
        this.validatorMixin = new BlankValidatorMixin(this, this.errorHandlerMixin.getErrorHandler());
    }

    public ValidationValueListBox(Renderer<T> renderer, ProvidesKey<T> keyProvider) {
        super(renderer, keyProvider);
        this.validatorMixin = new BlankValidatorMixin(this, this.errorHandlerMixin.getErrorHandler());
    }

    @Override
    public boolean getAllowBlank() {
        return this.validatorMixin.getAllowBlank();
    }

    @Override
    public void setAllowBlank(boolean allowBlank) {
        this.validatorMixin.setAllowBlank(allowBlank);
    }

    @Override
    public void showErrors(List<EditorError> errors) {
        this.errorHandlerMixin.showErrors(errors);
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandlerMixin.getErrorHandler();
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandlerMixin.setErrorHandler(handler);
    }

    @Override
    public ErrorHandlerType getErrorHandlerType() {
        return this.errorHandlerMixin.getErrorHandlerType();
    }

    @Override
    public void setErrorHandlerType(ErrorHandlerType type) {
        this.errorHandlerMixin.setErrorHandlerType(type);
    }

    @Override
    public void addValidator(Validator<T> validator) {
        this.validatorMixin.addValidator(validator);
    }

    @Override
    public boolean getValidateOnBlur() {
        return this.validatorMixin.getValidateOnBlur();
    }

    @Override
    public void reset() {
        this.setValue((T) null);
        this.validatorMixin.reset();
    }

    @Override
    public void setValidateOnBlur(boolean validateOnBlur) {
        this.validatorMixin.setValidateOnBlur(validateOnBlur);
    }

    @Override
    public void setValidators(Validator<T>... validators) {
        this.validatorMixin.setValidators(validators);
    }

    @Override
    public boolean validate() {
        return this.validatorMixin.validate();
    }

    @Override
    public boolean validate(boolean show) {
        return this.validatorMixin.validate(show);
    }

    @Override
    public HandlerRegistration addValidationChangedHandler(ValidationChangedEvent.ValidationChangedHandler handler) {
        return this.validatorMixin.addValidationChangedHandler(handler);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        errorHandlerMixin.clearErrors();
        super.setValue(value, fireEvents);
    }
}
