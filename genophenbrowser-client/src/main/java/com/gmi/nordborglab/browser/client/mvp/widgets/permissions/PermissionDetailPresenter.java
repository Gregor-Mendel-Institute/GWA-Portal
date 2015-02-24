package com.gmi.nordborglab.browser.client.mvp.widgets.permissions;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PermissionDoneEvent;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.gmi.nordborglab.browser.shared.proxy.PermissionPrincipalProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PermissionRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PermissionDetailPresenter extends
        PresenterWidget<PermissionDetailPresenter.MyView> implements PermissionUiHandlers {

    public interface MyView extends View, HasUiHandlers<PermissionUiHandlers> {

        PermissionDetailView.PermissionEditDriver getEditDriver();

        void addPermission(AccessControlEntryProxy permission);

        List<AccessControlEntryProxy> getPermissionList();

        void setShareUrl(String shareUrl);

        String getModuleUrl();

        void setAvailableUsersToSearch(List<AppUserProxy> users);

        void showModifiedNotificaitonPanel(boolean dirty);

        void setAccessControlEntryPlaceHolder(AccessControlEntryProxy newPermission);

        void resetUserSearchBox();

    }

    private SecureEntityProxy proxy;
    private CustomAclProxy acl;
    private final CustomRequestFactory rf;
    private Receiver<CustomAclProxy> receiver = null;
    private PermissionRequest editContext = null;
    private final PlaceManager placeManager;
    private final AccessControlEntryProxy newPermissionPlaceHolder;
    private final Map<String, AppUserProxy> userMap = Maps.newHashMap();


    @Inject
    public PermissionDetailPresenter(final EventBus eventBus, final MyView view,
                                     final CustomRequestFactory rf, final PlaceManager placeManager) {
        super(eventBus, view);
        getView().setUiHandlers(this);
        this.rf = rf;
        this.placeManager = placeManager;
        PermissionRequest ctx = rf.permissionRequest();
        this.newPermissionPlaceHolder = ctx.create(AccessControlEntryProxy.class);
        newPermissionPlaceHolder.setPrincipal(ctx.create(PermissionPrincipalProxy.class));
        newPermissionPlaceHolder.setMask(AccessControlEntryProxy.READ);
        newPermissionPlaceHolder.getPrincipal().setIsUser(true);
        receiver = new Receiver<CustomAclProxy>() {

            public void onSuccess(CustomAclProxy response) {
                fireEvent(new LoadingIndicatorEvent(false));
                acl = response;
                onEdit();
            }

            public void onFailure(ServerFailure error) {
                fireEvent(new LoadingIndicatorEvent(false));
                fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
                onEdit();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                super.onConstraintViolation(violations);
                fireEvent(new LoadingIndicatorEvent(false));
                //getView().setState(State.EDITING,getPermission());
            }
        };
    }

    @Override
    protected void onBind() {
        super.onBind();
        rf.permissionRequest().findAllUsers().fire(new Receiver<List<AppUserProxy>>() {
            @Override
            public void onSuccess(List<AppUserProxy> response) {
                for (AppUserProxy user : response) {
                    userMap.put(user.getId().toString(), user);
                }
                getView().setAvailableUsersToSearch(response);
                getView().setAccessControlEntryPlaceHolder(newPermissionPlaceHolder);
            }
        });
    }

    @Override
    protected void onReset() {
        super.onReset();
    }

    private String getShareUrl(String shareUrlToken) {
        return getView().getModuleUrl() + "#" + shareUrlToken;
    }

    public void setDomainObject(SecureEntityProxy proxy, final String shareUrlToken) {
        this.proxy = proxy;
        rf.permissionRequest().getPermissions(proxy).fire(new Receiver<CustomAclProxy>() {

            @Override
            public void onSuccess(CustomAclProxy response) {
                acl = response;
                getView().setShareUrl(getShareUrl(shareUrlToken));
                onEdit();
            }
        });
    }


    @Override
    public void onSave() {
        PermissionRequest ctx = (PermissionRequest) getView().getEditDriver().flush();
        ctx.fire();
        fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
    }

    public void onEdit() {
        editContext = rf.permissionRequest();
        editContext.updatePermissions(proxy, acl).to(receiver);
        getView().getEditDriver().edit(acl, editContext);
        getView().showModifiedNotificaitonPanel(false);
    }

    @Override
    public void onDelete(AccessControlEntryProxy object) {
        getView().getPermissionList().remove(object);
        getView().showModifiedNotificaitonPanel(true);
    }

    @Override
    public void onUpdatePermission(AccessControlEntryProxy value) {
        getView().showModifiedNotificaitonPanel(true);
    }

    @Override
    public void onCancel() {
        onEdit();
    }

    @Override
    public void onDone() {
        getEventBus().fireEventFromSource(new PermissionDoneEvent(proxy), this);
    }

    @Override
    public void onCancelAddUser() {
        resetNewPermissionHolder();
    }

    @Override
    public void onAddPermission(Set<String> users, AccessControlEntryProxy permissionToAdd) {
        boolean isModified = false;
        for (final String user : users) {

            AccessControlEntryProxy foundPermission = Iterables.find(getView().getPermissionList(), new Predicate<AccessControlEntryProxy>() {
                @Override
                public boolean apply(@Nullable AccessControlEntryProxy accessControlEntryProxy) {
                    if (accessControlEntryProxy == null)
                        return false;
                    return (accessControlEntryProxy.getPrincipal().getId().equals(user));
                }
            }, null);
            if (foundPermission == null) {
                AccessControlEntryProxy newPermission = editContext.create(AccessControlEntryProxy.class);
                PermissionPrincipalProxy principal = editContext.create(PermissionPrincipalProxy.class);
                principal.setId(user);
                principal.setIsUser(true);
                AppUserProxy appUser = userMap.get(user);
                principal.setName(getDisplayName(appUser));
                principal.setAvatarHash(appUser.getGravatarHash() + "?d=identicon" + (appUser.getAvatarSource() == AppUserProxy.AVATAR_SOURCE.IDENTICON ? "&f=1" : ""));
                newPermission.setPrincipal(principal);
                newPermission.setMask(newPermissionPlaceHolder.getMask());
                getView().getPermissionList().add(newPermission);
                isModified = true;
            }
        }
        getView().showModifiedNotificaitonPanel(isModified);
        getView().resetUserSearchBox();
        resetNewPermissionHolder();
    }

    private void resetNewPermissionHolder() {
        newPermissionPlaceHolder.setMask(AccessControlEntryProxy.READ);
        getView().setAccessControlEntryPlaceHolder(newPermissionPlaceHolder);
    }

    private String getDisplayName(AppUserProxy user) {
        return user.getFirstname() + " " + user.getLastname() + " (" + user.getEmail() + ")";
    }
}
