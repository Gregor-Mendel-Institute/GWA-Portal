package com.gmi.nordborglab.browser.client.mvp.presenter.main;

import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.events.UserChangeEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.AccountUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.view.main.AccountView;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.security.CurrentUser;
import com.gmi.nordborglab.browser.client.security.IsLoggedInGatekeeper;
import com.gmi.nordborglab.browser.client.validation.ClientValidation;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.UserRequest;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class AccountPresenter extends Presenter<AccountPresenter.MyView, AccountPresenter.MyProxy> implements AccountUiHandlers {

    public interface MyView extends View, HasUiHandlers<AccountUiHandlers> {

        AccountView.UserEditDriver getUserEditDriver();

        void setAvatarUrl(String url);

        void setIdenticonUrl(String url);

        void setGravatarImgUrl(String url);

        void setActiveAvatarSource(AppUserProxy.AVATAR_SOURCE source);
    }

    @ProxyStandard
    @NameToken(NameTokens.account)
    @UseGatekeeper(IsLoggedInGatekeeper.class)
    public interface MyProxy extends ProxyPlace<AccountPresenter> {
    }

    private AppUserProxy appUser;
    private AppUserProxy modifiedAppUserProxy;
    private final CurrentUser currentUser;
    private final PlaceManager placeManager;
    private final CustomRequestFactory rf;
    private final ClientValidation validation;
    private final Receiver<AppUserProxy> saveReceiver = new Receiver<AppUserProxy>() {

        @Override
        public void onSuccess(AppUserProxy response) {
            fireEvent(new LoadingIndicatorEvent(false));
            if (response.getId().equals(currentUser.getAppUser().getId())) {
                currentUser.setAppUser(response);
                // Fire change event
                getEventBus().fireEvent(new UserChangeEvent(appUser));
            }
            appUser = response;
            onEdit();
        }

        public void onFailure(ServerFailure error) {
            fireEvent(new LoadingIndicatorEvent(false));
            fireEvent(new DisplayNotificationEvent("Error while saving", error.getMessage(), true, DisplayNotificationEvent.LEVEL_ERROR, 0));
            onEdit();
        }

        public void onConstraintViolation(
                Set<ConstraintViolation<?>> violations) {
            fireEvent(new LoadingIndicatorEvent(false));
            getView().getUserEditDriver().setConstraintViolations(violations);
        }
    };

    @Inject
    public AccountPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                            final CurrentUser currentUser, final PlaceManager placeManager,
                            final CustomRequestFactory rf,
                            final ClientValidation validation) {
        super(eventBus, view, proxy, MainPagePresenter.TYPE_SetMainContent);
        getView().setUiHandlers(this);
        this.placeManager = placeManager;
        this.validation = validation;
        this.currentUser = currentUser;
        this.rf = rf;
    }

    @Override
    protected void onBind() {
        super.onBind();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onEdit();
        setActiveAvatar();
        getView().setGravatarImgUrl(CurrentUser.getGravatarUrl(modifiedAppUserProxy, 40, false));
        getView().setIdenticonUrl(CurrentUser.getGravatarUrl(modifiedAppUserProxy, 40, false) + "&f=1");
    }

    private void setActiveAvatar() {
        String avatarUrl = CurrentUser.getGravatarUrl(modifiedAppUserProxy, 200, true);
        getView().setAvatarUrl(avatarUrl);
        getView().setActiveAvatarSource(modifiedAppUserProxy.getAvatarSource());
    }


    @Override
    public void onSave() {
        RequestContext req = getView().getUserEditDriver().flush();
        if (onValidate()) {
            fireEvent(new LoadingIndicatorEvent(true, "Saving..."));
            req.fire();
        }
    }

    @Override
    public boolean onValidate() {
        getView().getUserEditDriver().flush();
        Set<ConstraintViolation<?>> constraintViolations = validation.getConstraintViolations(modifiedAppUserProxy, getView().getUserEditDriver().getErrors());
        if (!constraintViolations.isEmpty()) {
            getView().getUserEditDriver().setConstraintViolations(constraintViolations);
            return false;
        }
        return true;
    }

    @Override
    public void onSelectAvatarSource(AppUserProxy.AVATAR_SOURCE avatarSource) {
        // change to Editor interface
        modifiedAppUserProxy.setAvatarSource(avatarSource);
        setActiveAvatar();
    }

    @Override
    public void onCancel() {
        onEdit();
        setActiveAvatar();
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);
        // Check if user is logged in
        if (!currentUser.isLoggedIn()) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.home).build());
        }
        try {
            Long userId = Long.valueOf(placeRequest.getParameter("id",
                    currentUser.getAppUser().getId().toString()));
            // check if trying to load a userid of a different user and is not admin
            if (userId != null && !currentUser.getAppUser().getId().equals(userId) && !currentUser.isAdmin()) {
                appUser = null;
                getProxy().manualRevealFailed();
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.account).build());
                return;
            }
            Long userIdToLoad = mustReload(userId);
            if (userIdToLoad != null) {
                fireEvent(new LoadingIndicatorEvent(true));
                rf.userRequest().findUser(userIdToLoad).fire(new Receiver<AppUserProxy>() {
                    @Override
                    public void onSuccess(AppUserProxy response) {
                        appUser = response;
                        fireEvent(new LoadingIndicatorEvent(false));
                        getProxy().manualReveal(AccountPresenter.this);
                    }
                });
            } else {
                getProxy().manualReveal(AccountPresenter.this);
            }
        } catch (NumberFormatException e) {
            getProxy().manualRevealFailed();
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.home).build());
        }
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    private Long mustReload(Long userId) {
        if (appUser != null) {
            if (userId != null && userId.equals(appUser.getId())) {
                return null;
            } else if (userId == null && appUser.getId().equals(currentUser.getAppUser().getId())) {
                return null;
            }
        }
        return userId != null ? userId : currentUser.getAppUser().getId();
    }

    private void onEdit() {
        UserRequest ctx = rf.userRequest();
        // required because of http://stackoverflow.com/questions/9789870/requestfactoryeditordriver-getting-edited-data-after-flush
        modifiedAppUserProxy = ctx.edit(appUser);
        getView().getUserEditDriver().edit(modifiedAppUserProxy, ctx);
        ctx.saveUser(modifiedAppUserProxy).to(saveReceiver);

    }
}
