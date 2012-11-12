package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.acl.AclExperimentIdentity;

public interface AclExperimentIdentityRepository extends JpaRepository<AclExperimentIdentity, Long>{

}
