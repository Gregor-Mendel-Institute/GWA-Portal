package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.phenotype.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOfMeasureRepository extends
        JpaRepository<UnitOfMeasure, Long> {

}
