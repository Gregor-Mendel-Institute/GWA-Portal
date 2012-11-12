package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.acl.AclExperimentEntry;

public interface AclExperimentEntryRepository extends JpaRepository<AclExperimentEntry, Long> {

}
