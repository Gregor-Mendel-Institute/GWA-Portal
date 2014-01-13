package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.06.13
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */


public interface NewsRepository extends JpaRepository<NewsItem, Long> {

}
