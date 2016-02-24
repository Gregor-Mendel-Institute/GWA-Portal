package com.gmi.nordborglab.browser.client.security;

import com.gmi.nordborglab.browser.client.events.LoadStudyEvent;
import com.gmi.nordborglab.browser.client.events.LoadUserNotificationEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.AuthorityProxy;
import com.gmi.nordborglab.browser.shared.proxy.GWASRuntimeInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Set;


public class CurrentUser {

    private AppUserProxy appUser = null;
    private AppDataProxy appData = null;
    private final RequestDispatcher dispatcher;
    private final MessageBus messageBus;
    private final EventBus eventBus;
    private boolean isComChannelOpen = false;
    private final CustomRequestFactory rf;

    @Inject
    public CurrentUser(RequestDispatcher dispatcher, MessageBus messageBus, EventBus eventBus, CustomRequestFactory rf) {
        this.dispatcher = dispatcher;
        this.messageBus = messageBus;
        this.eventBus = eventBus;
        this.rf = rf;
    }

    public void setAppUser(AppUserProxy appuser) {
        this.appUser = appuser;
        if (appUser != null) {
            initComChanel();
        } else {
            closeComChanel();
        }
    }

    public int getUserId() {
        int userId = 0;
        if (getAppUser() != null && getAppUser().getId() != null) {
            userId = getAppUser().getId().intValue();
        }
        return userId;
    }

    public boolean isLoggedIn() {
        return appUser != null;
    }

    public AppUserProxy getAppUser() {
        return appUser;
    }

    public int getPermissionMask(AccessControlEntryProxy ace) {
        int permission = 0;
        if (isLoggedIn()) {
            if (ace != null) {
                permission = ace.getMask();
            }
        }
        return permission;
    }

    public boolean hasEdit(SecureEntityProxy entity) {
        if (entity == null || entity.getUserPermission() == null)
            return false;
        return hasEdit(entity.getUserPermission().getMask());
    }

    public boolean hasEdit(int permission) {
        boolean hasEdit = false;
        if (isLoggedIn()) {
            hasEdit = (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT || hasAdmin(permission);
        }
        return hasEdit;
    }


    public boolean hasAdmin(SecureEntityProxy entity) {
        if (entity == null || entity.getUserPermission() == null)
            return false;
        return hasAdmin(entity.getUserPermission().getMask());
    }

    public boolean hasAdmin(int permission) {
        boolean hasAdmin = false;
        if (isLoggedIn()) {
            hasAdmin = (permission & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION;
        }
        return hasAdmin;
    }

    public void setAppData(AppDataProxy appData) {
        this.appData = appData;
        addNullValues();
    }


    public AppDataProxy getAppData() {
        return appData;
    }

    private void addNullValues() {
        if (appData == null)
            return;
        appData.getUnitOfMeasureList().add(0, null);
        appData.getStatisticTypeList().add(0, null);
        appData.getStudyProtocolList().add(0, null);
        appData.getAlleleAssayList().add(0, null);
    }

    public Set<GWASRuntimeInfoProxy> getRuntimeInfoFromAlleleAssayId(final Long id) {
        return Sets.newHashSet(Iterables.filter(appData.getGWASRuntimeInfoList(), new Predicate<GWASRuntimeInfoProxy>() {
            @Override
            public boolean apply(GWASRuntimeInfoProxy input) {
                Preconditions.checkNotNull(input);
                return input.getAlleleAssayId().equals(id);
            }
        }));
    }

    public boolean isAdmin() {
        if (!isLoggedIn())
            return false;
        return isAdmin(appUser);
    }

    public static boolean isAdmin(AppUserProxy user) {
        if (user == null)
            return false;
        if (user.getAuthorities() == null)
            return false;
        AuthorityProxy authority = Iterables.find(user.getAuthorities(), new Predicate<AuthorityProxy>() {

            @Override
            public boolean apply(@Nullable AuthorityProxy input) {
                if (input == null)
                    return false;
                if (input.getAuthority().equals("ROLE_ADMIN"))
                    return true;
                return false;
            }
        }, null);
        if (authority == null)
            return false;
        return true;
    }

    private void closeComChanel() {
        messageBus.unsubscribeAll("UserNotificationReceiver");
        isComChannelOpen = false;
    }

    private void initComChanel() {
        if (isComChannelOpen || appUser == null)
            return;
        messageBus.subscribe("BroadcastReceiver", new MessageCallback() {
            @Override
            public void callback(Message message) {
                eventBus.fireEvent(new LoadUserNotificationEvent());
            }
        });

        messageBus.subscribe(appUser.getEmail(), new MessageCallback() {
            @Override
            public void callback(Message message) {
                String type = message.get(String.class, "type");
                if (type.equals("gwasjob")) {
                    Long id = message.get(Long.class, "id");
                    rf.cdvRequest().findStudy(id).with(CdvManager.FULL_PATH).fire(new Receiver<StudyProxy>() {
                        @Override
                        public void onSuccess(StudyProxy response) {
                            eventBus.fireEvent(new LoadStudyEvent(response));
                        }
                    });
                }
                eventBus.fireEvent(new LoadUserNotificationEvent());
            }
        });
        MessageBuilder.createMessage("ClientComService")
                .noErrorHandling()
                .sendNowWith(dispatcher);
    }


    public void updateNotificationCheckDate() {
        if (!isLoggedIn())
            return;
        Date date = new Date();
        MessageBuilder.createMessage("UpdateCheckNotificationDateService")
                .signalling()
                .withValue(date)
                .noErrorHandling()
                .sendNowWith(dispatcher);
    }

    public String getGravatarUrl(int size) {
        String url = getGravatarUrl(appUser, size, true);
        return url;
    }

    public static String getGravatarUrl(AppUserProxy user, int size, boolean check) {
        if (user == null)
            return "";
        String url = AppUserProxy.GRAVATAR_URL + user.getGravatarHash() + ".jpg?s=" + size + "&d=identicon";
        if (check) {
            url = url + (user.getAvatarSource() == AppUserProxy.AVATAR_SOURCE.IDENTICON ? "&f=1" : "");
        }
        return url;
    }
}
