package com.gmi.nordborglab.browser.client.manager;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.BreadcrumbItemProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.HelperRequest;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class HelperManager extends RequestFactoryManager<HelperRequest> {

	@Inject
	public HelperManager(CustomRequestFactory rf) {
		super(rf);
	}

	@Override
	public HelperRequest getContext() {
		return rf.helperRequest();
	}
	
	public void getBreadcrumbs(Receiver<List<BreadcrumbItemProxy>> receiver,Long id,String object) {
		getContext().getBreadcrumbs(id, object).fire(receiver);
	}

}
