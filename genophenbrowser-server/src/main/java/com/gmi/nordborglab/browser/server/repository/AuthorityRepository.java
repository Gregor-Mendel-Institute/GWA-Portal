package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmi.nordborglab.browser.server.domain.acl.Authority;
import com.gmi.nordborglab.browser.server.domain.acl.AuthorityPK;

public interface AuthorityRepository extends JpaRepository<Authority, AuthorityPK>{

}
