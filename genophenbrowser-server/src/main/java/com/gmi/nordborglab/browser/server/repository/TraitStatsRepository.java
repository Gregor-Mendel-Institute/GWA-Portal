package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.phenotype.TraitStats;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.Repository;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * Created by uemit.seren on 8/4/14.
 */
public interface TraitStatsRepository extends Repository<TraitStats, Long> {

    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    public List<TraitStats> findByTraitUomIdAndStatisticTypeId(Long phenotypeId, Long statiticTypeId);

}
