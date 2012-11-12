package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.form.Registration;

public interface UserService {
	
	void registerUserIfValid(Registration registration,boolean userIsValid) throws DuplicateRegistrationException;
}
