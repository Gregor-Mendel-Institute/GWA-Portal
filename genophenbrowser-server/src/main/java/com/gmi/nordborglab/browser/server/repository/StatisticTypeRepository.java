package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.phenotype.StatisticType;

public interface StatisticTypeRepository extends
		JpaRepository<StatisticType, Long> {

}
