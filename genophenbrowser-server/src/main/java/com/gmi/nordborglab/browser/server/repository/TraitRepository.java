package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TraitRepository extends JpaRepository<Trait, Long> {
	
	@Query("SELECT DISTINCT t FROM Trait t JOIN t.statisticType as s JOIN t.traitUom as uom JOIN t.obsUnit as o JOIN o.stock as sto JOIN sto.passport as pas WHERE EXISTS (SELECT asa from AlleleAssay as asa JOIN asa.alleles as al WHERE asa.id = :alleleAssayId )  AND uom.id = :phenotypeId AND (s.id = :statisticTypeId or :statisticTypeId is null) order by t.value DESC")
	List<Trait> findAllTraitValues(@Param("phenotypeId") Long phenotypeId,@Param("alleleAssayId") Long alleleAssayId,@Param("statisticTypeId") Long statisticTypeId);

	List<Trait> findByTraitUomIdAndStatisticTypeId(Long phenotypeId,Long statiticTypeId);
	
	List<Trait> findAllByStudiesId(Long studyId);

    @Query("SELECT COUNT(t) from Trait t WHERE t.traitUom.id = :phenotypeId AND t.statisticType.id = :statisticTypeId")
    Long countNumberOfTraitValues(@Param("phenotypeId") Long phenotypeId,@Param("statisticTypeId") Long statisticTypeId);

}
