package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnitSample;

public interface ObsUnitSampleRepository extends
		JpaRepository<ObsUnitSample, Long> {

}
