package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 3/20/13
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PublicationRepository extends JpaRepository<Publication,Long>{

    Publication findByDoi(String oid);
}
