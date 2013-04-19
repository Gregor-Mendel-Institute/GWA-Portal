package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.Notifcation;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.form.Registration;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {
	
	void registerUserIfValid(Registration registration,boolean userIsValid) throws DuplicateRegistrationException;

}
