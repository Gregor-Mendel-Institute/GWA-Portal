package com.gmi.nordborglab.browser.client.mvp.widgets.permissions;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.PermissionDoneEvent;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserPageProxy;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.CustomAclProxy;
import com.gmi.nordborglab.browser.shared.proxy.PermissionPrincipalProxy;
import com.gmi.nordborglab.browser.shared.proxy.SecureEntityProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PermissionRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class PermissionDetailPresenter extends
        PresenterWidget<PermissionDetailPresenter.MyView> implements PermissionUiHandlers {

    public interface MyView extends View, HasUiHandlers<PermissionUiHandlers> {

        PermissionDetailView.PermissionEditDriver getEditDriver();

        void addPermission(AccessControlEntryProxy permission);

        List<AccessControlEntryProxy> getPermissionList();

        void setShareUrl(String shareUrl);

        String getModuleUrl();

        void setAvailableUsersToSearch(Collection<AppUserProxy> users);

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
                DisplayNotificationEvent.fireError(getEventBus(), "Error while saving", "Failed to save experiment");
                onEdit();
            }

            public void onConstraintViolation(
                    Set<ConstraintViolation<?>> violations) {
                super.onConstraintViolation(violations);
                fireEvent(new LoadingIndicatorEvent(false));
            }
        };
    }

    @Override
    protected void onBind() {
        super.onBind();
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

    @Override
    public void onSearchUsers(String searchString, PermissionDetailView.SearchUserCallback callback) {
        rf.userRequest().findUsers(searchString, ConstEnums.USER_FILTER.ALL, 0, 25).fire(new Receiver<AppUserPageProxy>() {
            @Override
            public void onSuccess(AppUserPageProxy response) {
                callback.onDisplayResults(response.getContents());
            }
        });
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
    public void onAddPermission(List<AppUserProxy> users, AccessControlEntryProxy permissionToAdd) {
        boolean isModified = false;
        for (final AppUserProxy appUser : users) {

            AccessControlEntryProxy foundPermission = Iterables.find(getView().getPermissionList(), accessControlEntryProxy -> {
                if (accessControlEntryProxy == null)
                    return false;
                return (accessControlEntryProxy.getPrincipal().getId().equals(appUser));
            }, null);
            if (foundPermission == null) {
                AccessControlEntryProxy newPermission = editContext.create(AccessControlEntryProxy.class);
                PermissionPrincipalProxy principal = editContext.create(PermissionPrincipalProxy.class);
                principal.setId(appUser.getId().toString());
                principal.setIsUser(true);
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
