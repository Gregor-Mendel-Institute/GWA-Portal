package com.gmi.nordborglab.browser.shared.proxy;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName("com.gmi.nordborglab.browser.server.domain.pages.ExperimentPage")
public interface ExperimentPageProxy extends ValueProxy{
	List<ExperimentProxy> getContent();
	int getNumber();
	long getTotalElements();
	int getTotalPages();
}
