package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;

import java.util.List;

public interface TraitService {
    List<Trait> findAllTraitValues(Long phenotypeId, Long alleleAssayId, Long statisticTypeId);

    List<Trait> findAllTraitValuesByStatisticType(Long phenotypeId, Long statiticTypeId);
}
