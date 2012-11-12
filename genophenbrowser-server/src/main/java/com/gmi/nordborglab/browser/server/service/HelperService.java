package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;

public interface HelperService {

	List<BreadcrumbItem> getBreadcrumbs(Long id,String object);
	AppData getAppData();
}
