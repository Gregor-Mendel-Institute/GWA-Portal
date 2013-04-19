package com.gmi.nordborglab.browser.server.repository;

import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/17/13
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserNotificationRepository extends JpaRepository<UserNotification,Long>{


    List<UserNotification> findByAppUserUsernameLikeOrAppUserIsNullOrderByIdDesc(String username);
}
