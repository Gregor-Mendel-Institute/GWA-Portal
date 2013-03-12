package com.gmi.nordborglab.browser.client.validation;

import javax.validation.Validator;
import javax.validation.groups.Default;

import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.validation.client.AbstractGwtValidatorFactory;
import com.google.gwt.validation.client.GwtValidation;
import com.google.gwt.validation.client.impl.AbstractGwtValidator;

public class ClientValidatorFactory extends AbstractGwtValidatorFactory {
	
	
	
	public ClientValidatorFactory()  {
	}
	
    @GwtValidation(value = { StudyProxy.class,PhenotypeProxy.class,ExperimentProxy.class }, groups = {Default.class})
    public interface GwtValidator extends Validator {
    }

    @Override
    public AbstractGwtValidator createValidator() {
        return GWT.create(GwtValidator.class);
    }
}
