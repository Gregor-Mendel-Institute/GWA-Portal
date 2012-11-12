package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;

public interface UnitOfMeasureRepository extends
		JpaRepository<UnitOfMeasure, Long> {

}
