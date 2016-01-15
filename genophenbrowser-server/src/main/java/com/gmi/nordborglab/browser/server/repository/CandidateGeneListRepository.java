package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 23.09.13
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public interface CandidateGeneListRepository extends JpaRepository<CandidateGeneList, Long> {

    @Query("SELECT c.id FROM CandidateGeneList c JOIN c.candidateGeneListEnrichments e where e.study.id=:studyId")
    List<Long> findExistingEnrichmentByStudy(@Param("studyId") Long studyId);

}
