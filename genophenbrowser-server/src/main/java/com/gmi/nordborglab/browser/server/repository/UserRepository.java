package com.gmi.nordborglab.browser.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;


public interface UserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

    public AppUser findByEmail(String email);

    public AppUser findByUsername(String username);

    AppUser findByPasswordResetToken(String token);
}
