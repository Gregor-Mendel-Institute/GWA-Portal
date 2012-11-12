package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;

public interface AlleleAssayRepository extends JpaRepository<AlleleAssay, Long> {

}
