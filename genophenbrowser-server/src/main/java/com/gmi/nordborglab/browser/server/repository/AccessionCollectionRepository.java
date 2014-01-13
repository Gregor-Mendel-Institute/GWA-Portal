package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.germplasm.AccessionCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessionCollectionRepository extends
        JpaRepository<AccessionCollection, Long> {
}
