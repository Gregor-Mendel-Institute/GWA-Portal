package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/9/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface StudyJobRepository extends JpaRepository<StudyJob,Long>{

    List<StudyJob> findByStatusInAndTaskidIsNull(String... submitted);
}
