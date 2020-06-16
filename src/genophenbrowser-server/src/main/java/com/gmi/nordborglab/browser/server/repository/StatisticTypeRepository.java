package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticTypeRepository extends
        JpaRepository<StatisticType, Long> {

}
