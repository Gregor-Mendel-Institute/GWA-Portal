package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;

public interface CdvService {
	
	public StudyPage findStudiesByPhenotypeId(Long id,int start, int size);
	
	public Study findStudy(Long id);
	
	public Study saveStudy(Study study);
	
	public List<Study> findStudiesByPassportId(Long passportId);
	
}
