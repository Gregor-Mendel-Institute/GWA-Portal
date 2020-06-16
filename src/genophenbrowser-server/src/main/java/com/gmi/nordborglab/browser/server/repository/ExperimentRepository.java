package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {


    @Query("SELECT e FROM Experiment e JOIN e.obsUnits as o JOIN o.traits as trait JOIN trait.traitUom as t  WHERE t.id = :phenotypeId")
    public Experiment findByPhenotypeId(@Param("phenotypeId") Long phenotypeId);

}
