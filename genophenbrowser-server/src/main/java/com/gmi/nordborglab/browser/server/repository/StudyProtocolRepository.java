package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.cdv.StudyProtocol;

public interface StudyProtocolRepository extends JpaRepository<StudyProtocol, Long> {

}
