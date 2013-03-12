package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.GWASResultProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.ExperimentRequest;
import com.gmi.nordborglab.browser.shared.service.GWASDataRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWASDataManager extends RequestFactoryManager<GWASDataRequest>{

    @Inject
    public GWASDataManager(CustomRequestFactory rf) {
        super(rf);
    }

    @Override
    public GWASDataRequest getContext() {
        return rf.gwasDataRequest();
    }

    public void findAllGWASResults(Receiver<List<GWASResultProxy>> receiver) {
        getContext().findAllGWASResults().with("appUser").fire(receiver);
    }

    public void delete(Receiver<List<GWASResultProxy>> receiver, GWASResultProxy object) {
        getContext().delete(object).with("appUser").fire(receiver);
    }

    public void findOneGWASResults(Receiver<GWASResultProxy> receiver,Long id) {
        getContext().findOneGWASResult(id).fire(receiver);
    }
}
