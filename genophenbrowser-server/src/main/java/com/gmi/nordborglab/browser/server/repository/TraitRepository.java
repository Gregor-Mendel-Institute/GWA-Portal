package com.gmi.nordborglab.browser.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;

public interface TraitRepository extends JpaRepository<Trait, Long> {
	
	@Query("SELECT DISTINCT t FROM Trait t JOIN t.statisticType as s JOIN t.traitUom as uom JOIN t.obsUnit as o JOIN o.stock as sto JOIN sto.passport as pas WHERE EXISTS (SELECT asa from AlleleAssay as asa JOIN asa.alleles as al WHERE asa.id = :alleleAssayId )  AND uom.id = :phenotypeId AND (s.id = :statisticTypeId or :statisticTypeId is null) order by t.value DESC")
	List<Trait> findAllTraitValues(@Param("phenotypeId") Long phenotypeId,@Param("alleleAssayId") Long alleleAssayId,@Param("statisticTypeId") Long statisticTypeId);

	List<Trait> findByTraitUomIdAndStatisticTypeId(Long phenotypeId,Long statiticTypeId);

}
