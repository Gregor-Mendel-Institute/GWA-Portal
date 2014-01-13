package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnit;
import com.gmi.nordborglab.browser.server.domain.pages.ObsUnitPage;

import java.util.List;

public interface ObsUnitService {

    public ObsUnitPage findObsUnits(Long id, int start, int size);

    public List<ObsUnit> findObsUnitWithNoGenotype(Long phenotypeId, Long alleleAssayId);
}
