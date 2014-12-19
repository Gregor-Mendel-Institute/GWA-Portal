package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.12.13
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
@Repository
public interface CandidateGeneListEnrichmentRepository extends JpaRepository<CandidateGeneListEnrichment, Long> {
    List<CandidateGeneListEnrichment> findByStatusInAndTaskidIsNull(String... waiting);

    CandidateGeneListEnrichment findByTaskid(String task_id);
}
