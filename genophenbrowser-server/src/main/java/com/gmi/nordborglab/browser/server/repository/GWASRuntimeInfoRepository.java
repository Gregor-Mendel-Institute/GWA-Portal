package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.GWASRuntimeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by uemit.seren on 12/18/14.
 */
public interface GWASRuntimeInfoRepository extends JpaRepository<GWASRuntimeInfo, Long> {
}

