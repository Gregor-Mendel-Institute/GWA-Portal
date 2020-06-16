package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.observation.Locality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalityRepository extends JpaRepository<Locality, Long> {

}
