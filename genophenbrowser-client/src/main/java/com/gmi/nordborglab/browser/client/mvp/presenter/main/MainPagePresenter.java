package com.gmi.nordborglab.browser.client.mvp.presenter.main;

import com.gmi.nordborglab.browser.client.CurrentUser;
import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.events.DisplayNotificationEvent;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.mvp.handlers.MainUiHandlers;
import com.gmi.nordborglab.browser.shared.proxy.AppUserProxy;
import com.gmi.nordborglab.browser.shared.proxy.UserNotificationProxy;
import com.gmi.nordborglab.browser.shared.service.AppUserFactory;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.HelperRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentEvent;

import javax.annotation.Nullable;
import java.util.List;

public class MainPagePresenter extends
		Presenter<MainPagePresenter.MyView, MainPagePresenter.MyProxy> implements MainUiHandlers {

    public interface MyView extends View,HasUiHandlers<MainUiHandlers> {

        String getUserData();
        void showUserInfo(AppUserProxy user);
        void setActiveNavigationItem(MENU menu);
        void showNotification(String caption, String message, int level,
				int duration);
        void showLoadingIndicator(boolean show, String text);
        void refreshNotifications(List<UserNotificationProxy> notifications);

        void resetNotificationBubble();
    }
	@ContentSlot
	public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    public static final Object TYPE_SetUserInfoContent = new Object();
	@ProxyStandard
	public interface MyProxy extends Proxy<MainPagePresenter> {

    }
	private final UserInfoPresenter userInfoPresenter;

    private final AppUserFactory appUserFactory;
    protected final CurrentUser currentUser;
    private final PlaceManager placeManager;
    private final CustomRequestFactory rf;

    public enum MENU {HOME,DIVERSITY,GERMPLASM,GENOTYPE;}
	@Inject
	public MainPagePresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy,final UserInfoPresenter userInfoPresenter,
			final AppUserFactory appUserFactory,final CurrentUser currentUser,
			final PlaceManager placeManager,
            final CustomRequestFactory rf) {
		super(eventBus, view, proxy);
        getView().setUiHandlers(this);
		this.userInfoPresenter = userInfoPresenter;
		this.appUserFactory = appUserFactory;
		this.currentUser = currentUser;
		this.placeManager = placeManager;
        this.rf = rf;
	}


	@Override
	protected void revealInParent() {
		RevealRootLayoutContentEvent.fire(this, this);
	}

	@Override
	protected void onBind() {
		super.onBind();
		registerHandler(getEventBus().addHandler(DisplayNotificationEvent.getType(), new DisplayNotificationEvent.DisplayNotificationHandler() {

			@Override
			public void onDisplayNotifcation(DisplayNotificationEvent event) {
				getView().showNotification(event.getCaption(), event.getMessage(), event.getLevel(), event.getDuration());
			}
		}));

		registerHandler(getEventBus().addHandler(LoadingIndicatorEvent.getType(), new LoadingIndicatorEvent.LoadingIndicatorHandler() {

			@Override
			public void onProcessLoadingIndicator(LoadingIndicatorEvent event) {
				getView().showLoadingIndicator(event.getShow(),event.getText());
			}
		}));
       // registerHandler(getEventBus().addHandler(LoadNotificationEvent.getType(),new )
		//setInSlot(TYPE_SetUserInfoContent, userInfoPresenter);
	}

	private MENU getParentMenuFromRequest(PlaceRequest request) {
		MENU menu = MENU.HOME;
		if (request.matchesNameToken(NameTokens.experiment)  ||
			request.matchesNameToken(NameTokens.experiments) ||
			request.matchesNameToken(NameTokens.phenotypes)  ||
			request.matchesNameToken(NameTokens.phenotype) ||
			request.matchesNameToken(NameTokens.obsunit) ||
			request.matchesNameToken(NameTokens.studylist) ||
			request.matchesNameToken(NameTokens.study) ||
			request.matchesNameToken(NameTokens.studygwas) ||
			request.matchesNameToken(NameTokens.studyoverview) ||
			request.matchesNameToken(NameTokens.phenotypeoverview) ||
			request.matchesNameToken(NameTokens.ontologyoverview) ||
            request.matchesNameToken(NameTokens.gwasViewer)
			)
			menu = MENU.DIVERSITY;
		else if (request.matchesNameToken(NameTokens.taxonomies) ||
				request.matchesNameToken(NameTokens.taxonomy) ||
				request.matchesNameToken(NameTokens.passports) ||
				request.matchesNameToken(NameTokens.passport) ||
				request.matchesNameToken(NameTokens.stock)
			)
			menu = MENU.GERMPLASM;
		return menu;
	}

	@Override
	protected void onReset() {
		super.onReset();
		getView().showUserInfo(currentUser.getAppUser());
		PlaceRequest request = placeManager.getCurrentPlaceRequest();
		getView().setActiveNavigationItem(getParentMenuFromRequest(request));
        getView().refreshNotifications(currentUser.getAppData().getUserNotificationList());
	}

	@Override
	protected void onUnbind() {
		super.onUnbind();
		clearSlot(TYPE_SetUserInfoContent);
	}

    @Override
    public void onOpenAccountInfo() {
        if (currentUser.getAppData().getUserNotificationList() == null)
            return;
        if (Iterables.find(currentUser.getAppData().getUserNotificationList(), new Predicate<UserNotificationProxy>() {
            @Override
            public boolean apply(@Nullable UserNotificationProxy input) {
                return !input.isRead();
            }
        },null)!=null){
            currentUser.updateNotificationCheckDate();
        }
        getView().resetNotificationBubble();
    }

    @Override
    public void onCloseAccountInfo() {
        if (currentUser.getAppData().getUserNotificationList() == null)
            return;

        HelperRequest ctx = rf.helperRequest();
        for (UserNotificationProxy notification:currentUser.getAppData().getUserNotificationList()) {
            if (!notification.isRead()) {
                ctx.edit(notification);
                notification.setIsRead(true);
            }
        }
        getView().refreshNotifications(currentUser.getAppData().getUserNotificationList());
    }
}
