package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GWASResultRepository extends JpaRepository<GWASResult,Long> {

    @Query("SELECT g FROM GWASResult g WHERE g.appUser.username LIKE :username")
    List<GWASResult> findAllByUsername(@Param("username") String username);
}
