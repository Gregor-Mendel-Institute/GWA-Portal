package com.gmi.nordborglab.browser.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;

public interface HelperService {

	List<BreadcrumbItem> getBreadcrumbs(Long id,String object);
	AppData getAppData();

    PhenotypeUploadData getPhenotypeUploadData(byte[] inputStream) throws IOException;
}
