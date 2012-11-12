package com.gmi.nordborglab.browser.server.service;

import java.util.List;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;

public interface TraitService {
	List<Trait> findAllTraitValues(Long phenotypeId,Long alleleAssayId,Long statisticTypeId);
	
	List<Trait> findAllTraitValuesByStatisticType(Long phenotypeId,Long statiticTypeId);
}
