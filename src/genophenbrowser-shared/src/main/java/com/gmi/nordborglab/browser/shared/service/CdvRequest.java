package com.gmi.nordborglab.browser.shared.service;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value = "com.gmi.nordborglab.browser.server.service.CdvService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface CdvRequest extends RequestContext {
    Request<StudyPageProxy> findStudiesByPhenotypeId(Long id, int start, int size);

    Request<StudyProxy> findStudy(Long id);

    Request<StudyProxy> saveStudy(StudyProxy study);

    Request<List<StudyProxy>> findStudiesByPassportId(Long passportId);

    Request<StudyPageProxy> findAll(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<List<AlleleAssayProxy>> findAlleleAssaysWithStats(Long phenotypeId, Long statisticTypeId);

    Request<StudyProxy> createStudyJob(Long studyId);

    Request<Void> delete(StudyProxy study);

    Request<StudyPageProxy> findAll(Long id, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size);

    Request<StudyProxy> deleteStudyJob(Long id);

    Request<StudyProxy> rerunAnalysis(Long id);
}
