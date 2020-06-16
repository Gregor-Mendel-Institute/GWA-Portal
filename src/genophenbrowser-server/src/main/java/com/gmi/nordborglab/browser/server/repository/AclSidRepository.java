package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.acl.AclSid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AclSidRepository extends JpaRepository<AclSid, Long> {

    AclSid findBySid(String sid);

    List<AclSid> findAllBySidIn(Iterable<String> sids);

    AclSid findAllBySid(String sid);
}
