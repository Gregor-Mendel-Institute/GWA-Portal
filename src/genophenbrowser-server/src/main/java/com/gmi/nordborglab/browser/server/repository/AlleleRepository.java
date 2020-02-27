package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.genotype.Allele;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlleleRepository extends JpaRepository<Allele, Long> {

}
