package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.germplasm.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PassportRepository extends JpaRepository<Passport, Long>, QueryDslPredicateExecutor<Passport> {

    @Query("SELECT uom.toAccession,COUNT(p) from Passport p JOIN p.stocks as s JOIN s.obsUnits as o JOIN o.traits as t  JOIN t.traitUom as uom  WHERE p.id = :passportId  GROUP BY uom.toAccession ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerTraitOntology(@Param("passportId") Long passportId);

    @Query("SELECT uom.eoAccession,COUNT(p) from Passport p JOIN p.stocks as s JOIN s.obsUnits as o JOIN o.traits as t  JOIN t.traitUom as uom  WHERE p.id = :passportId  GROUP BY uom.eoAccession ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerEnvironmentOntology(@Param("passportId") Long passportId);

    @Query("SELECT st,COUNT(p) from Passport p JOIN p.stocks as s JOIN s.obsUnits as o JOIN o.traits as t  JOIN t.statisticType as st WHERE p.id = :passportId  GROUP BY st.id ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerStatisticType(@Param("passportId") Long passportId);

    @Query("SELECT uo,COUNT(p) from Passport p JOIN p.stocks as s JOIN s.obsUnits as o JOIN o.traits as t  JOIN t.traitUom as uom JOIN uom.unitOfMeasure as uo WHERE p.id = :passportId  GROUP BY uo.id ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerUnitOfMeasure(@Param("passportId") Long passportId);

}
 