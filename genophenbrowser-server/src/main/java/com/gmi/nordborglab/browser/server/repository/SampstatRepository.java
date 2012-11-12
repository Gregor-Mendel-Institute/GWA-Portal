package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.germplasm.Sampstat;

public interface SampstatRepository extends JpaRepository<Sampstat, Long> {

}
