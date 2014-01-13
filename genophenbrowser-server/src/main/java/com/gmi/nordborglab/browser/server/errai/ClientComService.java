package com.gmi.nordborglab.browser.server.errai;

import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.protocols.Resources;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 4/17/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */


@Service
public class ClientComService implements MessageCallback {

    private static Multimap<String, String> session2UserMap = ArrayListMultimap.create();

    private static RequestDispatcher dispatcher;

    @Inject
    public ClientComService(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void callback(Message message) {
        String username = SecurityUtil.getUsername();
        QueueSession sess = message.getResource(QueueSession.class, Resources.Session.name());
        String sessionId = sess.getSessionId();
        session2UserMap.put(username, sessionId);
    }

    public static void pushBroadcastNotification(String... usernames) {
        try {
            List<String> sessionIds = Lists.newArrayList();
            if (usernames != null && usernames.length > 0) {
                for (int i = 0; i < usernames.length; i++) {
                    if (!session2UserMap.containsKey(usernames[i])) {
                        sessionIds.addAll(session2UserMap.get(usernames[i]));
                    }
                }
            }
            if (sessionIds.size() == 0) {
                MessageBuilder.createMessage("BroadcastReceiver")
                        .signalling()
                        .noErrorHandling()
                        .sendNowWith(dispatcher);
            } else {
                for (String sessionId : sessionIds) {
                    try {
                        MessageBuilder.createMessage("BroadcastReceiver")
                                .signalling()
                                .with(MessageParts.SessionID, sessionId)
                                .noErrorHandling()
                                .sendNowWith(dispatcher);
                    } catch (Exception e) {
                    }
                }
            }

        } catch (Exception e) {
        }
    }

    public static void pushUserNotification(String username, String subject, String type, Long id) {
        if (username != null && !username.isEmpty()) {
            if (!session2UserMap.containsKey(username))
                return;
        }
        try {
            MessageBuilder.createMessage(subject)
                    .signalling()
                    .with("type", type)
                    .with("id", id)
                    .noErrorHandling()
                    .sendNowWith(dispatcher);
        } catch (Exception e) {
        }
    }


}
