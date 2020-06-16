package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.observation.ObsUnitSample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObsUnitSampleRepository extends
        JpaRepository<ObsUnitSample, Long> {

}
