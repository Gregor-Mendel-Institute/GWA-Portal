package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.genotype.ScoringTechType;

public interface ScoringTechTypeRepository extends JpaRepository<ScoringTechType,Long> {

}
