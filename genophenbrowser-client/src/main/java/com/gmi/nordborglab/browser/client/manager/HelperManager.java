package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.HelperRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.List;

public class HelperManager extends RequestFactoryManager<HelperRequest> {

    @Inject
    public HelperManager(CustomRequestFactory rf) {
        super(rf);
    }

    @Override
    public HelperRequest getContext() {
        return rf.helperRequest();
    }

    public void getBreadcrumbs(Receiver<List<BreadcrumbItemProxy>> receiver, Long id, String object) {
        getContext().getBreadcrumbs(id, object).fire(receiver);
    }


    public void calculateTransformations(Receiver<List<TransformationDataProxy>> receiver, List<TraitProxy> values, Long alleleAssayId) {
        getContext().calculateTransformations(values, alleleAssayId).fire(receiver);
    }

    public void calculatePseudoHeritability(Receiver<Double> receiver, List<TraitProxy> traits, TransformationDataProxy.TYPE type, Long alleleAssayId) {
        getContext().calculatePseudoHeritability(traits, type, alleleAssayId).fire(receiver);
    }

}
