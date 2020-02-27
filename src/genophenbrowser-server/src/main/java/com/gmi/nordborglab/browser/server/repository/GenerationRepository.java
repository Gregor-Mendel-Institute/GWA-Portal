package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.germplasm.Generation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, Long> {

}
