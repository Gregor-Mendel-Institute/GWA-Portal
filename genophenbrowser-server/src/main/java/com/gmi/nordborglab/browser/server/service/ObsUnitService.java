package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.ObsUnitPage;

public interface ObsUnitService {

	public ObsUnitPage findObsUnits(Long id,int start, int size);
	
	public List<ObsUnit> findObsUnitWithNoGenotype(Long phenotypeId,Long alleleAssayId);
}
