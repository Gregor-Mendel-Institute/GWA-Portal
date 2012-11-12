package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.genotype.PolyType;

public interface PolyTypeRepository extends JpaRepository<PolyType, Long> {

}
