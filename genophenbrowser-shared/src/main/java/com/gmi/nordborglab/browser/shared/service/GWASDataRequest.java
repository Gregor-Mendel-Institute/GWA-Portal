package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 8:57 PM
 * To change this template use File | Settings | File Templates.
 */

@ServiceName(value="com.gmi.nordborglab.browser.server.service.GWASDataService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface GWASDataRequest extends RequestContext {
    Request<List<GWASResultProxy>> findAllGWASResults();

    Request<List<GWASResultProxy>> delete(GWASResultProxy object);
}
