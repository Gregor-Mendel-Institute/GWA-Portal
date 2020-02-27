package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.cdv.Transformation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/20/13
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TransformationRepository extends JpaRepository<Transformation,Long>{
}
