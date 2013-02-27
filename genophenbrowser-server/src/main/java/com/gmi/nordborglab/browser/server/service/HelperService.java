package com.gmi.nordborglab.browser.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.domain.phenotype.TransformationData;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

public interface HelperService {

	List<BreadcrumbItem> getBreadcrumbs(Long id,String object);
	AppData getAppData();

    PhenotypeUploadData getPhenotypeUploadData(byte[] inputStream) throws IOException;

    List<TransformationData> calculateTransformations(List<Double> values);

}
