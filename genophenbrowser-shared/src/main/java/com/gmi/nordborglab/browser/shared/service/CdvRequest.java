package com.gmi.nordborglab.browser.shared.service;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value="com.gmi.nordborglab.browser.server.service.CdvService",locator="com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface CdvRequest extends RequestContext {
	Request<StudyPageProxy> findStudiesByPhenotypeId(Long id,int start,int size);

	Request<StudyProxy> findStudy(Long id);
	
	Request<StudyProxy> saveStudy(StudyProxy study);
	
	Request<List<StudyProxy>> findStudiesByPassportId(Long passportId);
}
