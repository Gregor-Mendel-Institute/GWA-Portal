package com.gmi.nordborglab.browser.server.errai;

import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.util.AppContextManager;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/19/13
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class UpdateCheckNotificationDateService implements MessageCallback {

    private UserRepository userRepository;

    @Override
    public void callback(Message message) {
        if (userRepository == null) {
            ApplicationContext ctx = AppContextManager.getContext();
            userRepository = ctx.getBean(UserRepository.class);
        }
        AppUser appUser = userRepository.findOne(SecurityUtil.getUsername());
        appUser.setNotificationCheckDate(message.get(Date.class,MessageParts.Value));
        userRepository.save(appUser);
    }

}
