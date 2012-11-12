package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.germplasm.StockParent;

public interface StockParentRepository extends JpaRepository<StockParent, Long> {

}
