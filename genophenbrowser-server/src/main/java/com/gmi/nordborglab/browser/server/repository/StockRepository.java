package com.gmi.nordborglab.browser.server.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gmi.nordborglab.browser.server.domain.germplasm.Stock;
import com.gmi.nordborglab.browser.server.domain.germplasm.StockParent;

public interface StockRepository extends JpaRepository<Stock, Long> {

	public List<Stock> findAllByPassportId(Long passportId,Sort sort);
	
	@Query(nativeQuery=true,value="WITH RECURSIVE recursetree(div_stock_parent_id,div_stock_id, div_parent_id, role, recurrent) AS ( SELECT div_stock_parent.div_stock_parent_id,div_stock_id, div_parent_id,role, recurrent FROM germplasm.div_stock_parent WHERE div_stock_id = :stockId UNION ALL  SELECT t.div_stock_parent_id,t.div_stock_id, t.div_parent_id, t.role, t.recurrent FROM germplasm.div_stock_parent t  JOIN recursetree rt ON rt.div_parent_id = t.div_stock_id ) SELECT * FROM recursetree")
	public List<Object[]> findAncestors(@Param("stockId") Long stockId);

	@Query(nativeQuery=true,value="WITH RECURSIVE recursetree(div_stock_parent_id,div_stock_id, div_parent_id, role, recurrent) AS ( SELECT div_stock_parent.div_stock_parent_id,div_stock_id, div_parent_id,role, recurrent FROM germplasm.div_stock_parent WHERE div_parent_id = :stockId UNION ALL  SELECT t.div_stock_parent_id,t.div_stock_id, t.div_parent_id, t.role, t.recurrent FROM germplasm.div_stock_parent t  JOIN recursetree rt ON rt.div_stock_id = t.div_parent_id ) SELECT * FROM recursetree")
	public List<Object[]> findDescendents(@Param("stockId") Long stockId);
	
}
