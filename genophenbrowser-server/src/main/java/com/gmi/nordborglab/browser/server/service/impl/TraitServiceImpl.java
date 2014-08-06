package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitStats;
import com.gmi.nordborglab.browser.server.repository.TraitRepository;
import com.gmi.nordborglab.browser.server.repository.TraitStatsRepository;
import com.gmi.nordborglab.browser.server.service.TraitService;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TraitServiceImpl implements TraitService {

    @Resource
    protected RoleHierarchy roleHierarchy;

    @Resource
    protected MutableAclService aclService;

    @Resource
    protected TraitRepository traitRepository;

    @Resource
    protected TraitStatsRepository traitStatsRepository;

    @Override
    public List<Trait> findAllTraitValues(Long phenotypeId, Long alleleAssayId, Long statisticTypeId) {
        return traitRepository.findAllTraitValues(phenotypeId, alleleAssayId, statisticTypeId);
    }

    @Override
    public List<Trait> findAllTraitValuesByStatisticType(Long phenotypeId,
                                                         Long statiticTypeId) {
        return traitRepository.findByTraitUomIdAndStatisticTypeId(phenotypeId, statiticTypeId);
    }

    @Override
    public List<TraitStats> findTraitStatsByStatisticType(Long phenotypeId, Long statiticTypeId) {
        return traitStatsRepository.findByTraitUomIdAndStatisticTypeId(phenotypeId, statiticTypeId);
    }

}
