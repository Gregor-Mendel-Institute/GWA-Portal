package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.Notifcation;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.pages.AppUserPage;
import com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.pages.TraitUomPage;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.form.Registration;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {

    void registerUserIfValid(Registration registration, boolean userIsValid) throws DuplicateRegistrationException;

    AppUser findUser(Long id);

    AppUser findUserWithStats(Long id);

    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and #user.id == authentication.principal.id)")
    AppUser saveUser(AppUser user);

    ExperimentPage findExperiments(Long userId, int start, int size);

    TraitUomPage findPhenotypes(Long userId, int start, int size);

    StudyPage findStudies(Long userId, int start, int size);

    AppUserPage findUsers(String searchString, ConstEnums.USER_FILTER filter, int start, int size);

}
