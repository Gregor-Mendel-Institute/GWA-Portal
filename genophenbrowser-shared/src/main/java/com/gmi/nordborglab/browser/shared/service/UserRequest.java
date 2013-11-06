package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 31.10.13
 * Time: 16:15
 * To change this template use File | Settings | File Templates.
 */
@ServiceName(value = "com.gmi.nordborglab.browser.server.service.UserService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface UserRequest extends RequestContext {
    Request<AppUserProxy> findUser(Long id);

    Request<AppUserProxy> findUserWithStats(Long id);

    Request<AppUserProxy> saveUser(AppUserProxy user);

    Request<ExperimentPageProxy> findExperiments(Long userId, int start, int size);

    Request<PhenotypePageProxy> findPhenotypes(Long userId, int start, int size);

    Request<StudyPageProxy> findStudies(Long userId, int start, int size);
}
