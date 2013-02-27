package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;

import java.util.List;

public interface CdvService {
	
	public StudyPage findStudiesByPhenotypeId(Long id,int start, int size);
	
	public Study findStudy(Long id);
	
	public Study saveStudy(Study study);
	
	public List<Study> findStudiesByPassportId(Long passportId);
	
	public StudyPage findAll(String name,String phenotype,String experiment,Long alleleAssayId, Long stduyProtocolId,int start,int size);
	
	public List<Trait> findTraitValues(Long studyId);

    public List<AlleleAssay> findAlleleAssaysWithStats(Long phenotypeId, Long statisticTypeId);
	
}
