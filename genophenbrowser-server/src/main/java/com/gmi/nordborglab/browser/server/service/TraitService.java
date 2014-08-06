package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitStats;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TraitService {

    @PreAuthorize("hasPermission(#phenotypeId,'com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom','READ')")
    List<Trait> findAllTraitValues(Long phenotypeId, Long alleleAssayId, Long statisticTypeId);

    @PreAuthorize("hasPermission(#phenotypeId,'com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom','READ')")
    List<Trait> findAllTraitValuesByStatisticType(Long phenotypeId, Long statiticTypeId);

    @PreAuthorize("hasPermission(#phenotypeId,'com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom','READ')")
    List<TraitStats> findTraitStatsByStatisticType(Long phenotypeId, Long statiticTypeId);
}
