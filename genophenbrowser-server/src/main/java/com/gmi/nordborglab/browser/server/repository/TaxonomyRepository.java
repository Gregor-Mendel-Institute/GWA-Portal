package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.germplasm.Taxonomy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaxonomyRepository extends JpaRepository<Taxonomy, Long> {

    @Query("SELECT assay from Taxonomy t JOIN t.passports AS p JOIN p.alleles AS a JOIN a.alleleAssay AS assay WHERE t.id = :taxonomyId group by assay")
    List<AlleleAssay> findAlleleAssaysForTaxonomy(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT s,COUNT(p) from Taxonomy t JOIN t.passports as p JOIN p.sampstat as s WHERE t.id =  :taxonomyId GROUP BY s ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerSampStat(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT ala,COUNT(p) from Taxonomy  t JOIN t.passports as p JOIN p.alleles as al  JOIN al.alleleAssay AS ala  WHERE t.id = :taxonomyId GROUP BY ala ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerAlleleAssay(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT l.country,COUNT(p) from Taxonomy t JOIN t.passports as p JOIN p.collection AS c JOIN c.locality as l  WHERE t.id = :taxonomyId and l.country IS NOT NULL and l.country <> '' GROUP BY l.country ORDER BY COUNT(p)")
    List<Object[]> countPassportsPerCountry(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT g.comments,COUNT(s) from Taxonomy t JOIN t.passports as p JOIN p.stocks AS s JOIN s.generation as g  WHERE t.id = :taxonomyId  GROUP BY g.comments ORDER BY COUNT(p)")
    List<Object[]> countStocksPerGeneration(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT COUNT(p) from Passport p  WHERE p.taxonomy.id=:taxonomyId")
    long countPassports(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT COUNT(s) FROM Taxonomy t JOIN t.passports as p JOIN p.stocks s WHERE t.id=:taxonomyId")
    long countStocks(@Param("taxonomyId") Long taxonomyId);

    @Query("SELECT COUNT(uom.id) FROM TraitUom uom WHERE uom.id in (SELECT DISTINCT tr.traitUom.id FROM Trait tr JOIN tr.obsUnit o JOIN o.stock s JOIN s.passport p WHERE p.taxonomy.id=:taxonomyId)")
    long countPhenotypes(@Param("taxonomyId") Long taxonomyId);
}

