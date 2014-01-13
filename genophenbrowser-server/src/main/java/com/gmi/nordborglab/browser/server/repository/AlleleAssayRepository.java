package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlleleAssayRepository extends JpaRepository<AlleleAssay, Long> {

    @Query("SELECT COUNT(t) FROM Trait t JOIN t.obsUnit o JOIN o.stock s JOIN s.passport p JOIN p.alleles a JOIN a.alleleAssay WHERE t.traitUom.id = :phenotypeId AND t.statisticType.id = :statisticTypeId and a.alleleAssay.id = :alleleAssayId")
    Long countAvailableAlleles(@Param("phenotypeId") Long phenotypeId, @Param("statisticTypeId") Long statisticTypeId, @Param("alleleAssayId") Long alleleAssayId);
}
