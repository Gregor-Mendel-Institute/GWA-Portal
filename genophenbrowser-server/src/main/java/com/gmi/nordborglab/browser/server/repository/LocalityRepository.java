package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.observation.Locality;

public interface LocalityRepository extends JpaRepository<Locality, Long> {

}
